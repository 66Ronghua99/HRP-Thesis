
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
                    if l in self.normals.nodelist:
                        self.score[l] = self.score[l] + 1
                        self.sybils.add_score(listeners[k], listeners[k+1], l)
                        self.sybils.add_score(listeners[k+1], listeners[k], l)
            pass

    def _report_sybils(self, listeners, senders):
        victims = self.sybils.select_victim(senders)
        if len(victims) == 0:
            return
        for k in range(0, len(listeners), 2):
            if k + 1 == len(listeners):
                break
            if self.normals.is_this_type(listeners[k]) and self.normals.is_this_type(listeners[k+1]):
                for victim in victims:
                    self.score[victim] = self.score[victim] + 1
                    self.normals.add_score(listeners[k], listeners[k + 1], victim)
                    self.normals.add_score(listeners[k + 1], listeners[k], victim)

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
            self._report_sybils(listeners, senders)

    def score_display(self, type="N"):
        temp = []
        print("Total scores:", self.score)
        if type == "N":
            for i in self.normals.nodelist:
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
        print("Malicious:", self.sybils.malicious.nodelist)
        pass

    def hunt(self):
        normal_list = list(range(number_of_nodes))
        score_list = self.score.copy()
        # max = 0
        # for i in range(len(self.score)):
        #     if self.score[i] > max:
        #         max = self.score[i]
        # if max <= 5:
        #     #need to check the other method.
        #     if:
        #       the we can find some nodes can be detected as sybils, continue on the other methods
        #     else:
        #       return to this method
        while True:
            node = 0
            max = 0
            for i in range(len(score_list)):
                if score_list[i] > max:
                    max = score_list[i]
                    node = i
            if max <= 2:
                break
            suspect = None
            if node in self.normals.nodelist:
                suspect = self.normals.score_dict
            if node in self.sybils.nodelist:
                suspect = self.sybils.score_dict
            for company_id in suspect[node]: # go through to find all scores ought to be subtracted
                company_dict = suspect[node][company_id]
                for node_id, node_score in company_dict.items():
                    score_list[node_id] = score_list[node_id] - node_score
                suspect[company_id].pop(node)
            score_list[node] = 0
            normal_list.remove(node)

        print("Detection results:", normal_list, len(normal_list))


    def judgement(self):
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
    plt.title('Sybil percent:' + str(sybil_percent*100) + "%/Total nodes:" + str(number_of_nodes)
              + "/Malicious:" + str(m_count))
    plt.xlabel("score")
    plt.ylabel("node number")
    plt.legend()
    plt.show()


number_of_nodes = 16
sybil_percent = 0.4
m_count = 1
normal_scores = []
sybil_scores = []

if __name__ == '__main__':
    model = Model(count=number_of_nodes, m_count=m_count)

    for i in range(2000):
        model.shuffle()
        sybil_id = model.sybils.nodelist[0]
        normal_id = model.normals.nodelist[0]
        model.iteration(0, 0, number_of_nodes)
        model.report()
        # print(node.graph)
        score = model.score_display()
        model.hunt()
        normal_scores.append(score[normal_id])
        sybil_scores.append(score[sybil_id])

        print()
    normal_scores.sort()
    sybil_scores.sort()
    # plot_scores(normal_scores)
    # plot_scores(sybil_scores)
    plot_both(normal_scores, sybil_scores)
