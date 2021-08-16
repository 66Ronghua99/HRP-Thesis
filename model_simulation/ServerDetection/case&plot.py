from matplotlib import transforms

from ServerDetection.server import Server
from ServerDetection.child_servers import AllCombinationServer, NRoundServer
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
    server = NRoundServer(len(s_list) + len(n_list) + len(m_list))
    server.sentry_record = sentry_record
    server.score_list = score
    # server.rnd = server.node_num
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
        plt.xlabel("number of nodes", fontsize=18)
        plt.ylabel("Sybil eviction rate/%", fontsize=18)
        plt.legend(prop={'size': 17})
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
        plt.xlabel("number of nodes", fontsize=18)
        plt.ylabel("rate/%", fontsize=18)
        plt.legend(prop={'size': 15})
        plt.show()


def remove_last(ls):
    for l in ls:
        l.pop()


def plot_avg_score():
    avg_n = None
    avg_s = None
    counter = 0
    with open("average_comparison.txt", "r") as file:
        for line in file:
            line.strip()
            if counter == 0:
                avg_n = ast.literal_eval(line)
            else:
                avg_s = ast.literal_eval(line)
            counter += 1
    x_axis = list(range(10, 41, 5))
    print(avg_n, avg_s)
    plt.bar([i - 0.5 for i in x_axis], avg_n, 1, color='b', label="normal nodes")
    plt.bar([i + 0.5 for i in x_axis], avg_s, 1, color='r', label="Sybil nodes")
    plt.xticks(x_axis, x_axis)
    plt.xlabel("Percentage of Sybil nodes/%", fontsize=15)
    plt.ylabel("Average score of nodes/%", fontsize=15)
    plt.axhline(48, 0, 40, color='y', linestyle='--', linewidth=2)
    plt.text(5.6, 47, "48", size="medium")
    plt.legend(prop={'size': 15})
    plt.show()


def diff_ap_average_score_plot():
    avg_n = [[], [], []]
    avg_s = [[], [], []]
    counter = 0
    ctr = 0
    with open("ap_diff_score.txt", "r") as file:
        for line in file:
            line.strip()
            if counter % 2 == 0:
                avg_n[ctr] = ast.literal_eval(line)
            else:
                avg_s[ctr] = ast.literal_eval(line)
                ctr += 1
            counter += 1
    x_axis = list(range(10, 24))
    print(avg_n, "\n", avg_s)

    for j in range(3):
        plt.bar([i - 0.1 for i in x_axis], [float(i) for i in avg_n[j]], 0.2, color='b', label="normal nodes")
        plt.bar([i + 0.1 for i in x_axis], [float(i) for i in avg_s[j]], 0.2, color='r', label="Sybil nodes")
        plt.xticks(x_axis, x_axis)
        plt.xlabel("Number of nodes", fontsize=15)
        plt.ylabel("Average score", fontsize=15)
        plt.legend(prop={'size': 15})
        plt.show()
        plt.clf()


def diff_ap_eviction_rate():
    ctr1 = 0
    ctr2 = 0
    evic_n = [[], [], []]
    evic_s = [[], [], []]
    with open("eviction_rate.txt", "r+") as file:
        for line in file:
            line.strip()
            if line == "\n":
                continue
            if ctr1 % 2 == 0:
                evic_n[ctr2] = ast.literal_eval(line)
            else:
                evic_s[ctr2] = ast.literal_eval(line)
                ctr2 += 1
            ctr1 += 1
    print(evic_n)
    print(evic_s)
    x_axis = list(range(10, 24))
    for j in range(3):
        plt.plot(x_axis, [float(i)*100 for i in evic_n[j]], "o-", color='b', label="normal nodes")
        plt.plot(x_axis, [100 - float(i)*100 for i in evic_s[j]], "o-", color='r', label="sybil nodes")
        plt.xticks(x_axis, x_axis[::1])
        plt.xlabel("number of nodes", fontsize=18)
        plt.ylabel("Elimination rate/%", fontsize=14)
        plt.legend(prop={'size': 17})
        plt.show()
        plt.clf()


def diff_impersonate_rate_plot():
    fn = None
    fp = None
    error_rates = None
    ctr = 0
    with open("error_rate_result_2.txt", "r+") as file:
        for line in file:
            line.strip()
            if line == "\n" or line == "":
                continue
            if ctr == 0:
                fn = ast.literal_eval(line)
            if ctr == 1:
                fp = [float(i) for i in ast.literal_eval(line)]
            else:
                error_rates = ast.literal_eval(line)
            ctr += 1
    plt.plot([float(i)*100 for i in error_rates], [float(i)*100 for i in fn], "o-", color='b', label="false negative")
    plt.plot([float(i)*100 for i in error_rates], [float(i)*100 for i in fp], "o-", color='r', label="false positive")
    plt.xticks([float(i)*100 for i in error_rates])
    plt.xlabel("Impersonation rate/%", fontsize=18)
    plt.ylabel("FN/FP rate/%", fontsize=18)
    plt.legend(prop={'size': 17})
    plt.show()


def round_comparison_plot():
    ctr1 = 0
    ctr2 = 0
    log_evic_n = [[], [], []]
    log_evic_s = [[], [], []]
    n_evic_n = [[], [], []]
    n_evic_s = [[], [], []]
    with open("round_comparison.txt", "r+") as file:
        for line in file:
            line.strip()
            if line == "\n":
                continue
            if ctr1 % 4 == 0:
                log_evic_n[ctr2] = ast.literal_eval(line)
            elif ctr1 % 4 == 1:
                log_evic_s[ctr2] = ast.literal_eval(line)
            elif ctr1 % 4 == 2:
                n_evic_n[ctr2] = ast.literal_eval(line)
            elif ctr1 % 4 == 3:
                n_evic_s[ctr2] = ast.literal_eval(line)
                ctr2 += 1
            ctr1 += 1
    x_axis = list(range(10, 24))
    for j in range(3):
        plt.plot(x_axis, [float(i)*100 for i in log_evic_n[j]], "o-", color='b', label="2*log(n) rounds normal nodes")
        plt.plot(x_axis, [100 - float(i)*100 for i in log_evic_s[j]], "o-", color='r', label="2*log(n) rounds sybil nodes")
        plt.plot(x_axis, [float(i)*100 for i in n_evic_n[j]], "x-", color='g', label="N rounds normal nodes")
        plt.plot(x_axis, [100 - float(i)*100 for i in n_evic_s[j]], "x-", color='y', label="N rounds sybil nodes")
        plt.xticks(x_axis, x_axis[::1])
        plt.xlabel("number of nodes", fontsize=18)
        plt.ylabel("Elimination rate/%", fontsize=14)
        plt.legend(prop={'size': 17})
        plt.show()
        plt.clf()


def misbehavior_rate_plot():
    ctr1 = 0
    ctr2 = 0
    evic_n = [[], [], []]
    evic_s = [[], [], []]
    with open("framing_ratio2.txt", "r+") as file:
        for line in file:
            line.strip()
            if line == "\n":
                continue
            if ctr1 % 2 == 0:
                evic_n[ctr2] = ast.literal_eval(line)
            else:
                evic_s[ctr2] = ast.literal_eval(line)
                ctr2 += 1
            ctr1 += 1
    print(evic_n)
    print(evic_s)
    x_axis = list(range(0, 101, 10))
    y_axis = list(range(0, 101, 10))
    for j in range(3):
        plt.plot(x_axis, [float(i)*100 for i in evic_n[j]], "o-", color='b', label="normal nodes")
        plt.plot(x_axis, [100 - float(i)*100 for i in evic_s[j]], "o-", color='r', label="sybil nodes")
        plt.xticks(x_axis, x_axis)
        plt.yticks(y_axis, y_axis)
        plt.xlabel("Misbehavior rate/%", fontsize=17)
        plt.ylabel("Elimination rate/%", fontsize=17)
        plt.legend(prop={'size': 17})
        plt.show()
        plt.clf()


if __name__ == '__main__':
    pass
    # diff_impersonate_rate_plot()
    # diff_ap_eviction_rate()
    # round_comparison_plot()
    misbehavior_rate_plot()
    # diff_ap_average_score_plot()
    # server_test()
    # case_test()
    # cal_std_mean([0,0,0,0,0,0,0,0,0,0,7,7])
    # plot_sentry_comparison()
    # plot_eviction_comparison()
    # plot_avg_score()
