import json
import time

from flask import Blueprint
from flask import request

ctr = Blueprint("ctr", __name__)

from services.ca_service import sign_csr, save_and_auth, auth_and_sign


@ctr.route("/crt/csr/test", methods=["POST"])
def certificate_request_test():
    request_data = request.get_json()
    if not request_data:
        return_dict = {"return_code": 5004, "return_info": "require json data"}
        return return_dict
    response = sign_csr(request_data)
    return json.dumps(response, ensure_ascii=False)


@ctr.route("/crt/csr", methods=["POST"])
def cert_request():
    request_data = request.get_json()
    if not request_data:
        return_dict = {"return_code": 5004, "return_info": "require json data"}
        return return_dict
    save_and_auth(request_data)
    return json.dumps({"return_code": 200, "return_info": "successful"})


@ctr.route("/crt/auth", methods=["POST"])
def cert_auth():
    request_data = request.get_json()
    if not request_data:
        return_dict = {"return_code": 5004, "return_info": "require json data"}
        return return_dict
    response = auth_and_sign(request_data)
    return json.dumps(response, ensure_ascii=True)
