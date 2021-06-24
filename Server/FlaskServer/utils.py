import base64


def base64encode(input):
    return base64.b64encode(input).decode()


def base64decode(input):
    return base64.b64decode(input)