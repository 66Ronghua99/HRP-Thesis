from ServerDetection.model import Model
from ServerDetection.utils import statistics
from ServerDetection.log import log
fp: [] = []
fn: [] = []
n_total: [] = []
s_total: [] = []


def basic():
    for i in range(2000):
        model = Model(10, 0.4, 0)
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


def data_collection(server="server"):
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
    log(fn, file="sentry_comparison.txt")
    log(n_total, file="sentry_comparison.txt")
    log(fp, file="sentry_comparison.txt")
    log(s_total, file="sentry_comparison.txt")
    log()
    data_clear()


if __name__ == '__main__':
    # basic()
    data_collection()
    data_collection(server="server2")
