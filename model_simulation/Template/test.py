import numpy as np
import model


class Point(object):
    def __init__(self):
        pass

    def __del__(self):
        pass


def iteration(rnd, beg, end, graph):
    if end > beg + 1 and rnd < 8:
        mid = int((beg + end) / 2)
        for i in range(beg, mid):
            # select nodes to be listeners, and report all broadcasting normal nodes;
            # 1 stands for broadcast 0 stands for listener
            graph[i][rnd + 1] = 1
            pass
        for i in range(mid, end):
            graph[i][rnd] = 1
            pass
        iteration(rnd + 2, beg, mid, graph)
        iteration(rnd + 2, mid, end, graph)


if __name__ == '__main__':
    # normals = [0, 1, 2, 4, 5, 6, 8, 10, 11, 12, 14, 15]
    # sybils = [7, 9, 13]
    # malicious = [3]
    # nodes = list(range(16))
    # graph = np.zeros((16, 8), dtype=int).tolist()
    # iteration(0, 0, 16, graph)
    # i = 0
    # for line in graph:
    #     print(line, i)
    #     i = i + 1
    #
    # for j in range(len(graph[0])):
    #     senders, listeners = [], []
    #     for i in range(len(graph)):
    #         if graph[i][j] == 1:
    #             senders.append(i)
    #         else:
    #             listeners.append(i)
    #     print(senders, listeners)
    # model.sample_test([3, 4, 6, 7, 8, 9, 10, 13, 15], [11, 14, 2, 5, 12, 0], [1])
    model.sample_test([1, 2, 5, 6, 8, 9, 10, 14, 15], [3, 13, 7, 0, 12, 11], [4])
    print()


# TODO: deal with the sybils who get less report but report honest nodes multiple time
# Normals: [1, 2, 4, 5, 6, 10, 11, 12, 15]
# Sybils: [14, 13, 7, 8, 0, 9]
# Malicious: [3]
# Total scores: [2, 1, 2, 0, 2, 2, 3, 3, 3, 3, 1, 1, 1, 4, 3, 2]
# Normal scores: [1, 2, 2, 2, 3, 1, 1, 1, 2]
# Detection results: [0, 1, 3, 7, 8, 9, 10, 11, 12, 14] [1, 1, -1, 0, -1, -1, -1, 1, 1, 1, 1, 1, 1, -3, 1, -1] 10


# Normals: [1, 2, 5, 6, 8, 9, 10, 14, 15]
# Sybils: [3, 13, 7, 0, 12, 11]
# Malicious: [4]
# Total scores: [3, 2, 3, 5, 0, 1, 2, 5, 1, 1, 2, 1, 2, 3, 1, 1]
# Normal scores: [2, 3, 1, 2, 1, 1, 2, 1, 1]
# Detection results: [1, 4, 5, 6, 8, 9, 10, 11, 14, 15] [-1, 0, -4, -1, 0, 0, 0, -1, 0, 0, 0, 1, -1, -1, 0, 0] 10

