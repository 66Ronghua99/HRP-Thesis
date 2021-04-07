import random
import math
import numpy as np
import matplotlib.pyplot as plt
from node import Node, Sybil, Malicious
import node


# The simulation model
class Model(object):
    def __init__(self, count, m_count):
        self.counts = count
        self.sybils = Sybil(m_count) #init sybil object
        self.normals = Node() #init normal nodes object
        self.table = [] # broadcasting and listening order table
        self.score = [] # record the score of each node
        # Will be reconstruct later
        self.round = 2 * int(math.log(count, 2)) # rounds
        print("Rounds:", self.round)
        pass

    # Forming the broadcasting table
    def iteration(self, rnd, beg, end):
        if end > beg + 1 and rnd < self.round:
            mid = int((beg + end) / 2)
            for i in range(beg, mid):
                # select nodes to be listeners, and report all broadcasting normal nodes;
                # 1 stands for broadcast 0 stands for listener
                self.table[i][rnd + 1] = 1
                pass
            for i in range(mid, end):
                self.table[i][rnd] = 1
                pass
            self.iteration(rnd + 2, beg, mid)
            self.iteration(rnd + 2, mid, end)
        pass

    def _report_normals(self, listeners, senders):
        for k in range(0, len(listeners), 2):
            if k + 1 == len(listeners):
                break
            if (self.sybils.is_this_type(listeners[k]) or self.sybils.malicious.is_this_type(listeners[k])) and \
                    (self.sybils.is_this_type(listeners[k + 1]) or self.sybils.malicious.is_this_type(
                        listeners[k + 1])):
                for l in senders:
                    if l in self.normals.nodelist:
                        self.score[l] = self.score[l] + 1
                        self.sybils.add_score(listeners[k], listeners[k + 1], l)
                        self.sybils.add_score(listeners[k + 1], listeners[k], l)
            pass

    def _report_sybils(self, listeners, senders):
        victims = self.sybils.select_victim(senders)
        if len(victims) == 0:
            return
        for k in range(0, len(listeners), 2):
            if k + 1 == len(listeners):
                break
            if self.normals.is_this_type(listeners[k]) and self.normals.is_this_type(listeners[k + 1]):
                for victim in victims:
                    self.score[victim] = self.score[victim] + 1
                    self.normals.add_score(listeners[k], listeners[k + 1], victim)
                    self.normals.add_score(listeners[k + 1], listeners[k], victim)

    # Report procedure by sentries
    def report(self):
        # go through the process, normal and Sybils report the opponents when they should
        for j in range(self.round):
            listeners = []
            senders = []
            for i in range(self.counts):
                if self.table[i][j] == 0:
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

    # Randomly select nodes to be Sybils or normals
    def shuffle(self):
        nodelist = list(range(self.counts))
        self.sybils.clear()
        self.normals.clear()
        self.table = np.zeros((self.counts, self.round), dtype=int).tolist()
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

    # Hunt Sybils based on the score list (self.score)
    def hunt(self):
        normal_list = list(range(number_of_nodes)) # Nodes remain in the list are considered normal
        score_list = self.score.copy() # score list, the score will be set to -1 if the node is eliminated
        # TODO: if we get no score higher than threshold, do sth.....
        while True:
            node = 0
            max = 0
            # find the node with the highest score
            for i in range(len(score_list)):
                if score_list[i] > max:
                    max = score_list[i]
                    node = i
            if max <= 1:
                break
            suspect = Node.score_dict
            if suspect:
                for company_id in suspect[node]:  # go through to find all scores ought to be subtracted
                    company_dict = suspect[node][company_id]
                    score_sum = 0
                    for node_id, node_score in company_dict.items():
                        score_sum = score_sum + node_score
                        score_list[node_id] = score_list[node_id] - node_score
                    if score_list[company_id] >= 0:
                        score_list[company_id] = score_list[company_id] + score_sum
                    suspect[company_id].pop(node)
            score_list[node] = -1
            normal_list.remove(node)
        if len(normal_list) < number_of_nodes / 2:  # This is a test. If over half of the nodes are eliminated, eliminated nodes will be selected instead.
            for i in range(number_of_nodes): # this reverse selection is useless. Selected nodes will be half normal half Sybil.
                if i in normal_list:
                    normal_list.remove(i)
                else:
                    normal_list.append(i)
        print("Detection results:", normal_list, score_list, len(normal_list))
        for x in self.normals.nodelist:
            if x not in normal_list:
                print("False elimination")
                return False
        return True

    def judgement(self):
        pass


# This is for the tests of specific cases, not used in normal runs
def sample_test(normals, sybils, malicious):
    model = Model(number_of_nodes, m_count)
    model.normals = Node()
    model.sybils = Sybil(m_count)
    for node in normals:
        model.normals.add_node(node)
    for node in sybils:
        model.sybils.add_node(node)
    for node in malicious:
        model.sybils.score_dict[node] = {}
    model.table = np.zeros((model.counts, model.round), dtype=int).tolist()
    model.score = np.zeros(model.counts, dtype=int).tolist()
    model.sybils.malicious = Malicious(malicious)
    model.iteration(0, 0, number_of_nodes)
    print(model.table)
    model.report()
    model.hunt()


def plot_scores(scores, color='b', bar_width=0.0, name_label='normal'):
    mark = -1
    labels = []
    label_score = []
    total = len(scores)
    for score in scores:
        if score == mark:
            label_score[-1] = label_score[-1] + 1
        else:
            mark = score
            labels.append(mark)
            label_score.append(1)
    label_score = [float(x) * 100 / total for x in label_score]
    plt.bar(labels, label_score, alpha=0.7, color=color, width=0.5, label=name_label, tick_label=labels)
    # plt.show()
    return plt


def plot_both(normal, sybils):
    mark = -1
    labels = []
    label_score = []
    plot_scores(normal)
    plot_scores(sybils, color='r', name_label='sybils', bar_width=0.5)
    plt.title('Sybil percent:' + str(sybil_percent * 100) + "%/Total nodes:" + str(number_of_nodes)
              + "/Malicious:" + str(m_count))
    plt.xlabel("score")
    plt.ylabel("node percentage/%")
    plt.legend()
    plt.show()


def print_average(normal, sybils):
    pass
    total = 0
    for s in normal:
        total = total + s
    avg = float(total / node_selected)
    total1 = 0
    for s in sybils:
        total1 = total1 + s
    avg1 = float(total1 / node_selected)
    print("normals:", avg, "sybils: ", avg1)


number_of_nodes = 16
sybil_percent = 0.4
node_selected = 2000  # time of runs
m_count = 1 # malicious node number
normal_scores = []
sybil_scores = []

if __name__ == '__main__':
    model = Model(count=number_of_nodes, m_count=m_count)
    wrong_case = 0.0
    for i in range(node_selected):
        model.shuffle()
        sybil_id = model.sybils.nodelist[0]
        normal_id = model.normals.nodelist[0]
        model.iteration(0, 0, number_of_nodes)
        model.report()
        # print(node.graph)
        score = model.score_display()
        if not model.hunt():
            wrong_case = wrong_case + 1 # collecting wrong cases of elimination
        normal_scores.append(score[normal_id])
        sybil_scores.append(score[sybil_id])
        print(node.all_broadcast)
        node.all_broadcast = 0
        print()
    print("Wrong_case:", str(wrong_case * 100 / float(node_selected)) + "%")
    normal_scores.sort()
    sybil_scores.sort()
    # plot_scores(normal_scores)
    # plot_scores(sybil_scores)
    plot_both(normal_scores, sybil_scores)
    print_average(normal_scores, sybil_scores)
