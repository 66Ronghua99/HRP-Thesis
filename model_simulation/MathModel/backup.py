import random
import math
import numpy as np
import matplotlib.pyplot as plt
from node import Node, Sybil, Malicious


# 40% of the nodes are sybils
class Model(object):
    def __init__(self, count, m_count):
        self.counts = count
        self.sybils = Sybil(m_count)
        self.normals = Node()
        self.graph = []
        self.score = []
        self.reveal_times = []
        # Will be reconstruct later
        self.round = 2 * int(math.log(count, 2))
        print("Rounds:", self.round)
        pass

    def iteration(self, rnd, beg, end):
        if end > beg + 1 and rnd < self.round:
            mid = int((beg + end) / 2)
            for i in range(beg, mid):
                # select nodes to be listeners, and report all broadcasting normal nodes;
                # 1 stands for broadcast 0 stands for listener
                self.graph[i][rnd + 1] = 1
                pass
            for i in range(mid, end):
                self.graph[i][rnd] = 1
                pass
            self.iteration(rnd + 2, beg, mid)
            self.iteration(rnd + 2, mid, end)
        pass

    def _report_normals(self, listeners, senders):
        for k in range(0, len(listeners), 2):
            if k + 1 == len(listeners):
                break
            if (self.sybils.is_this_type(listeners[k]) or self.sybils.malicious.is_this_type(listeners[k])) and \
                    (self.sybils.is_this_type(listeners[k + 1]) or self.sybils.malicious.is_this_type(listeners[k + 1])):
                for l in senders:
                    if l in self.normals:
                        self.score[l] = self.score[l] + 1
                        self.sybils.add_score(listeners[k], l)
                        self.sybils.add_score(listeners[k+1], l)
            pass

    def _report_sybils(self, sybil_id, listeners, senders, reveal_times=1):
        if self.reveal_times[sybil_id] >= reveal_times:
            return
        if sybil_id in senders:
            self.reveal_times[sybil_id] = self.reveal_times[sybil_id] + 1
            templist = []
            for k in self.sybils:
                if k in senders and k != sybil_id:
                    templist.append(k)
            victim = random.choice(self.sybils)
            for k in range(0, len(listeners), 2):
                if k + 1 == len(listeners):
                    break
                if listeners[k] in self.normals and listeners[k + 1] in self.normals:
                    self.score[sybil_id] = self.score[sybil_id] + 1
                    self.score[victim] = self.score[victim] + 1

    def report(self):
        for j in range(self.round):
            listeners = []
            senders = []
            for i in range(self.counts):
                if self.graph[i][j] == 0:
                    listeners.append(i)
                else:
                    senders.append(i)
            self._report_normals(listeners, senders)
            self._report_sybils(self.sybils[0], listeners, senders, reveal_times=reveal_times)

    def score_display(self, type="N"):
        temp = []
        print("Total scores:", self.score)
        if type == "N":
            for i in self.normals:
                temp.append(self.score[i])
            print("Normal scores:", temp)
        else:
            pass
        return self.score

    def shuffle(self):
        nodelist = list(range(self.counts))
        self.sybils.clear()
        self.normals.clear()
        self.graph = np.zeros((self.counts, self.round), dtype=int).tolist()
        self.score = np.zeros(self.counts, dtype=int).tolist()
        sybils = random.sample(range(self.counts), int(sybil_percent * self.counts))
        for i in sybils:
            self.sybils.add_node(i)
        for i in nodelist:
            if self.sybils.is_this_type(i):
                continue
            self.normals.add_node(i)
        self.sybils.select_malicious()
        print("Normals:", self.normals.nodelist)
        print("Sybils:", self.sybils.nodelist)
        pass


def plot_scores(scores, color='b', bar_width=0.0, name_label='normal'):
    mark = -1
    labels = []
    label_score = []
    for score in scores:
        if score == mark:
            label_score[-1] = label_score[-1] + 1
        else:
            mark = score
            labels.append(mark)
            label_score.append(1)
    plt.bar(labels, label_score, alpha=0.7, color=color, width=0.5, label=name_label, tick_label=labels)
    # plt.show()
    return plt


def plot_both(normal, sybils):
    mark = -1
    labels = []
    label_score = []
    plot_scores(normal)
    plot_scores(sybils, color='r', name_label='sybils', bar_width=0.5)
    plt.title('Sybil percent:' + str(sybil_percent*100) + "%")
    plt.legend()
    plt.show()


number_of_nodes = 16
sybil_percent = 0.33
m_count = 1
reveal_times = 5
normal_scores = []
sybil_scores = []

if __name__ == '__main__':
    model = Model(count=number_of_nodes, m_count=m_count)

    for i in range(1000):
        model.shuffle()
        sybil_id = model.sybils.nodelist[0]
        normal_id = model.normals.nodelist[0]
        model.iteration(0, 0, number_of_nodes)
        model.report()
        # print(node.graph)
        score = model.score_display()
        normal_scores.append(score[normal_id])
        sybil_scores.append(score[sybil_id])

        print()
    normal_scores.sort()
    sybil_scores.sort()
    # plot_scores(normal_scores)
    # plot_scores(sybil_scores)
    plot_both(normal_scores, sybil_scores)
