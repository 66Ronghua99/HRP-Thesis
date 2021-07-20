from ServerDetection.model import Model, NormalSybilModel
from ServerDetection.utils import statistics, score_statistics, method_statistics
from ServerDetection.log import log
from ServerDetection.server import Server
from ServerDetection.method_comparison import ComparisonServer, ScoreOnlyServer, AllCombinationServer, NRoundServer

fp: list = []
fn: list = []
n_total: list = []
s_total: list = []
n_score = []
s_score = []
avg_n = []
avg_s = []


def basic():
    for i in range(2000):
        model = Model(16, 0.4, 2)
        model.main_process()


def n_rounds_server_model():
    for i in range(2000):
        model = Model(10, 0.4, 1, server=5)
        model.main_process()


def normal_sybil_model():
    for i in range(2000):
        model = NormalSybilModel(16, 0.4, 0)
        model.main_process()


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


def score_comparison(server="server", filename="sentry_comparison.txt"):
    data_init()
    for i in range(10, 31):
        for j in range(100):
            model = Model(i, 0.4, 0, server=server)
            model.main_process()
            statistics(model.normals, model.sybils, model.server.normal_list, fn, fp)
            n_total[-1] += len(model.normals)
            s_total[-1] += len(model.sybils)
        n_total.append(0)
        s_total.append(0)
        fn.append(0)
        fp.append(0)
    n_total.pop()
    s_total.pop()
    fn.pop()
    fp.pop()
    logs(filename)
    data_clear()


def logs(filename):
    log(fn, file=filename)
    log(n_total, file=filename)
    log(fp, file=filename)
    log(s_total, file=filename)
    log()


def eviction_comparison(filename):
    n_total.append(0)
    s_total.append(0)
    for i in range(3):
        fn.append([0])
        fp.append([0])

    for i in range(10, 31):
        for j in range(100):
            model = Model(i, 0.4, 0, server="compare")
            model.main_process()
            method_statistics(model.normals, model.sybils, fn, fp, n_total, s_total, model.server.hunter_list)
        n_total.append(0)
        s_total.append(0)
        for i in range(3):
            fn[i].append(0)
            fp[i].append(0)

    n_total.pop()
    s_total.pop()
    for i in range(3):
        fn[i].pop()
        fp[i].pop()
    logs(filename)
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
    log(avg_n, file="average_comparison.txt")
    log(avg_s, file="average_comparison.txt")


if __name__ == '__main__':
    basic()
    # n_rounds_server_model()
    # normal_sybil_model()
    # score_comparison()
    # score_comparison(server="server2")
    # eviction_comparison("eviction_comparison.txt")
    # average_score_statistics()
