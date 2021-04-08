from ServerDetection.server import Server
from ServerDetection.utils import euclidean_d
import json
import ast
import numpy as np


def server_test():
    server = Server(8)
    rssi = {4: 1, 5: 1, 6: 99, 7: 43}
    rssi2 = {4: 0, 5: 1, 6: 9, 7: 23}
    rssi3 = {4: 1, 5: 1, 6: 59, 7: 31}
    rssi4 = {4: 1, 5: 1, 6: 13, 7: 7}
    server.collect(0, rssi)
    server.collect(1, rssi2)
    server.collect(2, rssi3)
    server.collect(3, rssi4)


def pythonify(json_data):
    sentry_record = {}
    for key, value in json_data.items():
        if isinstance(value, list):
            value = [pythonify(item) if isinstance(item, dict) else item for item in value]
        elif isinstance(value, dict):
            value = pythonify(value)
        try:
            newkey = int(key)
        except TypeError:
            pass
        sentry_record[newkey] = value
    return sentry_record


def case_test():
    s_list = []
    n_list = []
    sentry_record = {}
    score = []
    with open("sp_cases.txt", "r+") as file:
        for line in file:
            first_comma = line.index(":")
            line = line.strip()
            if line.startswith("Sybils"):
                s_list = ast.literal_eval(line[first_comma+2:])
            elif line.startswith("Normal"):
                n_list = ast.literal_eval(line[first_comma+2:])
            elif line.startswith("process"):
                score = ast.literal_eval(line[first_comma+2:])
            elif line.startswith("sentry"):
                j_string = json.loads(line[first_comma+2:])
                sentry_record = pythonify(j_string)
    print("Sybils:", s_list)
    print("Normals:", n_list)
    server = Server(len(s_list) + len(n_list))
    server.sentry_record = sentry_record
    server.score_list = score
    server.process_finished()
    server._add_task(exit)
    server.threads[0].join()


def cal_std_mean(ls):
    print(np.mean(ls))
    print(np.std(ls))
    print(np.mean(ls)+ 1.7*np.std(ls))


if __name__ == '__main__':
    pass
    # server_test()
    # case_test()
    cal_std_mean([0,0,0,0,0,0,0,0,0,0,7,7])

