from ServerDetection.server import Server
from ServerDetection.utils import euclidean_d
import json
import ast
import numpy as np
import matplotlib.pyplot as plt


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
            elif line.startswith("Malicious"):
                m_list = ast.literal_eval(line[first_comma+2:])
            elif line.startswith("process"):
                score = ast.literal_eval(line[first_comma+2:])
            elif line.startswith("sentry"):
                j_string = json.loads(line[first_comma+2:])
                sentry_record = pythonify(j_string)
    print("Sybils:", s_list)
    print("Normals:", n_list)
    print("Malicious:", m_list)
    server = Server(len(s_list) + len(n_list) + len(m_list))
    server.sentry_record = sentry_record
    server.score_list = score
    server.process_finished()
    server._add_task(exit)
    server.threads[0].join()


def cal_std_mean(ls):
    print(np.mean(ls))
    print(np.std(ls))
    print(np.mean(ls)+ 1.7*np.std(ls))


def plot_sentry_comparison():
    fn = None
    fp = None
    n_total = None
    s_total = None
    counter = 0
    ctr2 = 0
    with open("sentry_comparison.txt", "r") as file:
        for line in file:
            line = line.strip()
            if line == "":
                counter = 0
                continue
            if counter == 0:
                fn = ast.literal_eval(line)[:-1]
            elif counter == 1:
                n_total = ast.literal_eval(line)[:-1]
            elif counter == 2:
                fp = ast.literal_eval(line)[:-1]
            else:
                s_total = ast.literal_eval(line)[:-1]
                plot(fn, fp, n_total, s_total, ctr2)
                ctr2 += 1
            counter += 1
        plt.xlabel("number of nodes")
        plt.ylabel("Sybil eviction rate/%")
        plt.legend()
        plt.show()


def plot(fn, fp, n_total, s_total, ctr):
    fn_rate = []
    fp_rate = []
    number = []
    for i in range(len(fp)):
        fn_rate.append(100 * (1 - fn[i]/n_total[i]))
        fp_rate.append(100 * (1 - fp[i]/s_total[i]))
        number.append(int(i + 10))
    # plt.plot(number, fn_rate, "o-")
    plt.plot(number, fp_rate, "o-", label=labels[ctr])
    plt.xticks(number, number[::1])


labels = ["1 device, randomly pair sentries", "1 device, all possible sentries",
          "2 devices, randomly pair sentries", "2 devices, all possible sentries"]
labels2 = ["Sybil eviction rate", "honest eviction rate"]


def plot2(fn, fp, n_total, s_total):
    fn_rate = []
    fp_rate = []
    number = []
    for j in range(len(fp)):
        fn_rate.append([])
        fp_rate.append([])
        for i in range(len(fp[j])):
            fn_rate[j].append(100 * fn[j][i]/n_total[i])
            fp_rate[j].append(100 * (1 - fp[j][i]/s_total[i]))
            if len(number) == len(n_total):
                continue
            number.append(int(i + 10))
    # plt.plot(number, fn_rate, "o-")
    for ctr in range(len(fp_rate)):
        plt.plot(number, fp_rate[ctr], "o-", label=f'Method {ctr + 1} '+labels2[ctr])
        plt.plot(number, fn_rate[ctr], "s-", label=f'Method {ctr + 1} '+labels2[ctr])
    plt.xticks(number, number[::1])


def plot_eviction_comparison():
    fn = None
    fp = None
    n_total = None
    s_total = None
    counter = 0
    with open("eviction_comparison.txt", "r") as file:
        for line in file:
            line = line.strip()
            if line == "":
                counter = 0
                continue
            if counter == 0:
                fn = ast.literal_eval(line)[:-1]
                remove_last(fn)
            elif counter == 1:
                n_total = ast.literal_eval(line)[:-1]
            elif counter == 2:
                fp = ast.literal_eval(line)[:-1]
                remove_last(fp)
            else:
                s_total = ast.literal_eval(line)[:-1]
                plot2(fn, fp, n_total, s_total)
            counter += 1
        plt.xlabel("number of nodes")
        plt.ylabel("rate/%")
        plt.legend()
        plt.show()


def remove_last(ls):
    for l in ls:
        l.pop()


if __name__ == '__main__':
    pass
    # server_test()
    # case_test()
    # cal_std_mean([0,0,0,0,0,0,0,0,0,0,7,7])
    plot_sentry_comparison()
    plot_eviction_comparison()
