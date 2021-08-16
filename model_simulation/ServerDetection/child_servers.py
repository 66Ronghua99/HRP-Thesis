from copy import deepcopy
import copy
import numpy as np
from ServerDetection.server import Server


class AllCombinationServer(Server):

    def __init__(self, num=16):
        super().__init__(num)
        self.threshold = self.calculate_threshold()

    # All possible combinations of sentries are considered
    def _add_score(self):
        listeners = self.listeners.copy()
        broadcasters = self.broadcasters.copy()
        ptr1 = 0
        ptr2 = 1
        while True:
            id0 = listeners[ptr1]
            id1 = listeners[ptr2]
            ptr2 += 1
            if ptr2 == len(listeners):
                ptr1 += 1
                ptr2 = ptr1 + 1
            self._add_task(self.suspect, id0, id1,
                           self.rssi_list[id0], self.rssi_list[id1], listeners, broadcasters)
            if ptr1 + 1 == len(listeners):
                break

    def calculate_threshold(self):
        num = self.node_num
        rnd = self.rnd
        return (int(num/5) * (int(num/5) - 1))/2 * rnd/2


class NRoundServer(AllCombinationServer):
    def __init__(self, num=16):
        super().__init__(num)
        self.rnd = self.node_num
        self.rounds = np.zeros((self.node_num, self.rnd), dtype=int).tolist()
        self.n_rounds()
        pass

    def n_rounds(self):
        pvt = 0
        for i in range(len(self.rounds[0])):
            for j in range(len(self.rounds)):
                if j < self.node_num/2:
                    self.rounds[(pvt + j)%self.node_num][i] = 1
                else:
                    self.rounds[(j + pvt)%self.node_num][i] = 0
            pvt += 1


class ComparisonServer(AllCombinationServer):
    def __init__(self, num):
        super().__init__(num)
        self.hunter_list = []

    def init_hunters(self):
        self.hunter_list.append(
            ThresholdOnlyServer(self.score_list.copy(), deepcopy(self.sentry_record),
                                self.normal_list.copy(), self.node_num, self.rnd))
        self.hunter_list.append(
            PreviousEviction(self.score_list.copy(), deepcopy(self.sentry_record),
                             self.normal_list.copy(), self.node_num, self.rnd))
        # self.server_list.append(
        #     NewEviction(self.score_list.copy(), deepcopy(self.sentry_record), self.normal_list.copy()))

    def hunt(self):
        self.init_hunters()
        for hunter in self.hunter_list:
            hunter.hunt()


class ThresholdOnlyServer:
    def __init__(self, score_list, sentry_record, normal_list, num, rnd):
        self.score_list: list = score_list
        self.sentry_record: dict = sentry_record
        self.normal_list: list = normal_list
        self.node_num = num
        self.rnd = rnd
        self.threshold = None

    def calculate_threshold(self):
        num = self.node_num
        rnd = self.rnd
        return (int(num / 5) * (int(num / 5) - 1)) / 2 * rnd

    def hunt(self):
        self.threshold = self.calculate_threshold()
        for id in range(len(self.score_list)):
            if self.score_list[id] >= self.threshold:
                self.normal_list.remove(id)


class PreviousEviction(ThresholdOnlyServer):
    def hunt(self):
        self.threshold = self.calculate_threshold()
        while True:
            # find the node with the highest score
            highest_list = []
            if not self._get_highest_list(highest_list):
                break
            eliminate_list = highest_list
            self._whitewash_and_punish(eliminate_list[0])

    def _whitewash_and_punish(self, node):
        for company_id in self.sentry_record[node]:  # go through to find all scores ought to be subtracted
            company_dict = self.sentry_record[node][company_id]
            score_sum = 0
            for node_id, node_score in company_dict.items():
                score_sum = score_sum + node_score
                self.score_list[node_id] = self.score_list[node_id] - node_score
            if self.score_list[company_id] >= 0:
                self.score_list[company_id] = self.score_list[company_id] + score_sum
            self.sentry_record[company_id].pop(node)
        self.sentry_record[node].clear()
        self.score_list[node] = -1
        self.normal_list.remove(node)
        return True

    def _get_highest_list(self, highest_list):
        max_point = 0
        for i in range(len(self.score_list)):
            if self.score_list[i] > max_point:
                max_point = self.score_list[i]
        if max_point < self.threshold:
            return False
        for i in range(len(self.score_list)):
            if self.score_list[i] == max_point:
                highest_list.append(i)
        return True


class ScoreOnlyServer(AllCombinationServer):
    def hunt(self):
        print("Detection results:", self.normal_list, self.score_list, len(self.normal_list))
