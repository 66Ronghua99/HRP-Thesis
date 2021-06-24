import random
import threading
import time
import OpenSSL
import base64

from database import Cert, insert, Csr, get_csr, delete
from entities import sign_response
from main import mail, app
from flask_mail import Message
from utils import base64encode, base64decode

ca_cert = None
ca_private_key = None

with open("ca.crt", "r") as cert_file:
    c = cert_file.read()
    ca_cert = OpenSSL.crypto.load_certificate(OpenSSL.crypto.FILETYPE_PEM, c)

with open("ca.pem", "r") as key_file:
    k = key_file.read()
    ca_private_key = OpenSSL.crypto.load_privatekey(OpenSSL.crypto.FILETYPE_PEM, k)


def insert_test():
    user = Cert(username="ronghua", timeMillis=current_time_millis(),
                encodedCert="kasdnlkfjklwnalkdjfalksndf;lkjqwleknfsdf")
    insert(user)
    return "successful"


def sign_csr(request_data):
    response = sign_response.copy()
    username: str = request_data["username"]
    encoded_csr: str = request_data["encodedCsr"]
    csr = base64decode(encoded_csr)
    req = OpenSSL.crypto.load_certificate_request(OpenSSL.crypto.FILETYPE_ASN1, csr)
    cert = create_cert(req)
    encoded_cert = base64encode(OpenSSL.crypto.dump_certificate(OpenSSL.crypto.FILETYPE_ASN1, cert)) # dump cert to byte
    response["username"] = username
    response["encodedCert"] = encoded_cert
    response["return code"] = 200
    return response


def create_cert(req):
    cert = OpenSSL.crypto.X509()
    cert.set_serial_number(1)
    cert.gmtime_adj_notBefore(0) # cannot be neglected, otherwise it cannot be used to form a certificate
    cert.gmtime_adj_notAfter(300)
    cert.set_issuer(ca_cert.get_subject())
    cert.set_subject(req.get_subject())
    cert.set_pubkey(req.get_pubkey())
    cert.sign(ca_private_key, "sha256")
    return cert


##### save and send verification code to verify the email
def save_and_auth(request_data):
    letter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    rnd_code = ""
    for i in range(4):
        a = random.randint(0, 35)
        rnd_code += letter[a]
    csr = Csr(username=request_data["username"], encodedCsr=request_data["encodedCsr"],
              timeMillis=current_time_millis(), code=rnd_code.lower())
    insert(csr)
    msg = Message("Account Verification", recipients=[request_data["username"]])
    msg.body = f"The code is: \n\n{rnd_code} \n\nPlease verify it within 5 mins."
    send_mail(msg)
#############################

######## send email ############
def send_async_email(msg):
    with app.app_context():
        mail.send(msg)


def send_mail(msg):
    threading.Thread(target=send_async_email, args=[msg]).start()
#############


######## verify code and sign certificate
def auth_and_sign(request_data):
    response = sign_response.copy()
    username: str = request_data["username"]
    code: str = request_data["code"].lower()
    csrs = get_csr(username, code)
    if len(csrs) != 1:
        response["return code"] = 5001
        response["error"] = "username and code incompatible, code incorrect or wrong username"
        return response
    csr = csrs[0]
    delete(csr)
    csr = base64decode(csr.encodedCsr)
    req = OpenSSL.crypto.load_certificate_request(OpenSSL.crypto.FILETYPE_ASN1, csr)
    cert = create_cert(req)
    encoded_cert = base64encode(OpenSSL.crypto.dump_certificate(OpenSSL.crypto.FILETYPE_ASN1, cert)) # dump cert to byte
    insert(Cert(username=username, timeMillis=current_time_millis(), encodedCert=encoded_cert))
    response["username"] = username
    response["encodedCert"] = encoded_cert
    response["return code"] = 200
    return response
############

######## authenticate user http request ########
def request_auth(request_data):
    pass
###############################################

def get_pub_key(key):
    key = base64decode(key)
    return OpenSSL.crypto.load_publickey(OpenSSL.crypto.FILETYPE_ASN1, base64decode(key))


def current_time_millis():
    return round(time.time() * 1000)