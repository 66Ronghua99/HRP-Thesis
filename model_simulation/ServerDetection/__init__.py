import numpy as np

from ServerDetection.model import Model, NormalSybilModel, DiffRoundModel
from ServerDetection.utils import statistics, score_statistics, method_statistics
from ServerDetection.log import log
from ServerDetection.server import Server
from ServerDetection.child_servers import ComparisonServer, ScoreOnlyServer, AllCombinationServer, NRoundServer

error_rates: list = []
fp: list = []
fn: list = []
fp_n_round: list = []
fn_n_round: list = []
n_total: list = []
s_total: list = []
n_score = []
s_score = []
avg_n = []
avg_s = []


def basic():
    for i in range(2000):
        model = Model(16, 0.4, 1, error_rate=0.2)
        model.main_process()


def n_rounds_server_model():
    for i in range(2000):
        model = Model(16, 0.4, 2, server=5)
        model.main_process()


def normal_sybil_model():
    for i in range(2000):
        model = NormalSybilModel(16, 0.4, 2, frame_ratio=0.0, error_rate=0.1)
        model.main_process()


def error_rate_difference(filename="error_rate_result3.txt"):
    data_init()
    error_rate = 0.0
    for i in range(11):
        fn.append(0)
        fp.append(0)
        error_rates.append(error_rate)
        for j in range(1000):
            model = Model(16, 0.4, 2, error_rate=error_rate)
            model.main_process()
            statistics(model.normals, model.sybils, model.malicious, model.server.normal_list, fn, fp)
        error_rate += 0.01
    fn.pop()
    fp.pop()
    logs(filename)
    data_clear()


def data_init():
    fn.append(0)
    fp.append(0)
    n_total.append(0)
    s_total.append(0)


def data_clear():
    fn.clear()
    fp.clear()
    n_total.clear()
    s_total.clear()


def average_score(filename="ap_diff_score.txt"):
    data_init()
    for k in range(3):
        for i in range(10, 24):
            for j in range(200):
                model = Model(i, 0.4, k, server=4)
                model.main_process()
                score_statistics(model.server.score_list, model.normals, model.sybils, n_score, s_score)
            avg_n.append(n_score[0])
            avg_s.append(s_score[0])
            n_score.clear()
            s_score.clear()
        log(['{:.4f}'.format(i) for i in avg_n], file=filename)
        log(['{:.4f}'.format(i) for i in avg_s], file=filename)
        avg_s.clear()
        avg_n.clear()
    data_clear()


def logs(filename):
    # log(fn, file=filename)
    log((np.array(fn)/10000).tolist(), file=filename)
    # log(n_total, file=filename)
    # log(fp, file=filename)
    a = (np.array(fp)/6000).tolist()
    log(['{:.4f}'.format(i) for i in a], file=filename)
    log(error_rates, file=filename)
    # log(s_total, file=filename)
    log()


def eviction_comparison(filename="eviction_rate.txt"):
    for k in range(3):
        for i in range(10, 24):
            fn.append(0)
            fp.append(0)
            for j in range(100):
                model = Model(i, 0.4, k)
                model.main_process()
                statistics(model.normals, model.sybils, model.malicious, model.server.normal_list, fn, fp)
        log(['{:.4f}'.format(i) for i in (np.array(fn) / (100 * (i - int(i * 0.4)))).tolist()], file=filename)
        log(['{:.4f}'.format(i) for i in (np.array(fp) / (100 * int(i * 0.4))).tolist()], file=filename)
        log(file=filename)
        fn.clear()
        fp.clear()
    data_clear()


def average_score_statistics():
    for i in range(10, 41, 5):
        for j in range(50):
            model = Model(20, i / 100, server="score")
            model.main_process()
            server: Server = model.server
            score_statistics(server.score_list, model.normals, model.sybils, n_score, s_score)
        avg_n.append(n_score[0])
        avg_s.append(s_score[0])
        n_score.clear()
        s_score.clear()
    log(['{:.4f}'.format(i) for i in avg_n], file="average_comparison.txt")
    log(['{:.4f}'.format(i) for i in avg_s], file="average_comparison.txt")


def round_comparison(filename="round_comparison.txt"):
    fn.append([])
    fn.append([])
    fp.append([])
    fp.append([])
    for k in range(3):
        for i in range(10, 24):
            for l in range(2):
                fn[l].append(0)
                fp[l].append(0)
            for j in range(100):
                model = DiffRoundModel(i, 0.4, k)
                model.main_process()
                server_list = model.server_list
                for l in range(2):
                    statistics(model.normals, model.sybils, model.malicious, server_list[l].normal_list, fn[l], fp[l])
        for l in range(2):
            log(['{:.4f}'.format(i) for i in (np.array(fn[l]) / (100 * (i - int(i * 0.4)))).tolist()], file=filename)
            log(['{:.4f}'.format(i) for i in (np.array(fp[l]) / (100 * int(i * 0.4))).tolist()], file=filename)
            fn[l].clear()
            fp[l].clear()
    data_clear()
    pass


def misbehavior_comparison(filename="framing_ratio3.txt"):
    for k in range(3):
        error_rate = 0.0
        for j in range(11):
            fn.append(0)
            fp.append(0)
            for i in range(100):
                model = NormalSybilModel(16, 0.4, k, frame_ratio=error_rate, error_rate=0)
                model.main_process()
                statistics(model.normals, model.sybils, model.malicious, model.server.normal_list, fn, fp)
            error_rate += 0.1
        log(['{:.4f}'.format(i) for i in (np.array(fn) / (100 * (16 - int(16 * 0.4)))).tolist()], file=filename)
        log(['{:.4f}'.format(i) for i in (np.array(fp) / (100 * int(16 * 0.4))).tolist()], file=filename)
        fn.clear()
        fp.clear()
    data_clear()


if __name__ == '__main__':
    # basic()
    error_rate_difference()
    # n_rounds_server_model()
    # normal_sybil_model()
    # misbehavior_comparison()
    # average_score()
    # score_comparison(server="server2")
    # eviction_comparison()
    # average_score_statistics()
    # round_comparison()