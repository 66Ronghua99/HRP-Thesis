import json
import time

from flask import Blueprint
from flask import request

ctr = Blueprint("ctr", __name__)

from services.ca_service import sign_csr, save_and_auth, auth_and_sign, request_auth
from services.node_service import NodeServer

node_server = NodeServer()


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


# { "username": xxxx, "publicKey": xxxxx }
@ctr.route("/wait/register", methods=["POST"])
def wait_start():
    request_data = request.get_json()
    if not request_data:
        return_dict = {"return_code": 5004, "return_info": "require json data"}
        return return_dict
    request_auth(request_data)
    response = node_server.register(request_data)
    return json.dumps(response)


# { "username": xxxx, "hash": xxxxx }
@ctr.route("/wait/broadcast", methods=["POST"])
def wait_broadcaster():
    request_data = request.get_json()
    if not request_data:
        return_dict = {"return_code": 5004, "return_info": "require json data"}
        return return_dict
    request_auth(request_data)
    response = node_server.broadcaster_ready(request_data)
    return json.dumps(response)


# { "username": xxxx, "rssi": {xxxxx}, "hash": xxxxx }
@ctr.route("/wait/detect", methods=["POST"])
def wait_detector():
    request_data = request.get_json()
    if not request_data:
        return_dict = {"return_code": 5004, "return_info": "require json data"}
        return return_dict
    request_auth(request_data)
    response = node_server.detector_ready(request_data)
    return json.dumps(response)
