import math

sybils = []
normals = []


def iteration(table, rnd, beg, end):
    ending = len(table[0])
    if end > beg + 1 and rnd < ending:
        mid = int((beg + end) / 2)
        for i in range(beg, mid):
            # select nodes to be listeners, and report all broadcasting normal nodes;
            # 1 stands for broadcast 0 stands for listener
            table[i][rnd + 1] = 1
            pass
        for i in range(mid, end):
            table[i][rnd] = 1
        iteration(table, rnd + 2, beg, mid)
        iteration(table, rnd + 2, mid, end)


def euclidean_d(loc1, loc2):
    x1 = loc1[0]
    y1 = loc1[1]
    x2 = loc2[0]
    y2 = loc2[1]
    return format(math.sqrt(math.pow((x1-x2), 2) + math.pow((y1-y2), 2)), '.6f')


def false_negative(id0, id1, suspect):
    global normals
    if (id0 in normals or id1 in normals) and suspect in normals:
        return True
    return False


def set_s_n(s, n):
    global sybils, normals
    sybils = s
    normals = n

