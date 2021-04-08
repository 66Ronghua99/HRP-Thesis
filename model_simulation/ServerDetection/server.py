import math
import threading
import numpy as np
from ServerDetection.utils import false_negative
from ServerDetection.utils import iteration
import json


class Server(object):
    def __init__(self, num=16):
        self.node_num = num
        self.rnd = 2 * int(math.log(num, 2))
        self.score_list = np.zeros(self.node_num, dtype=int).tolist()
        self.rounds = np.zeros((self.node_num, self.rnd), dtype=int).tolist()
        self.normal_list = list(range(self.node_num))
        self.rssi_list = {}
        self.cur_rnd = 0
        self.listeners = []
        self.broadcasters = []
        self.sentry_record = {}
        self.threads = []
        self.msg_queue = Queue()
        self.init_thread()
        self.init_sertry_record()
        iteration(self.rounds, 0, 0, self.node_num)
        pass

    def thread_loop_task(self):
        while True:
            while not self.msg_queue.empty:
                task: Task = self.msg_queue.pop()
                task.run()
                pass

    def init_thread(self):
        td = threading.Thread(target=self.thread_loop_task)
        self.threads.append(td)
        td.start()

    def init_sertry_record(self):
        for id in range(self.node_num):
            self.sentry_record[id] = {}

    def _process_finished_task(self):
        print("process finished! Score list:", self.score_list)
        print("sentry record: ", json.dumps(self.sentry_record))

    # thread adding score task
    def suspect(self, node0, node1, rssi0, rssi1, listeners, broadcasters):
        ratios = {}
        suspects = {}
        for i in range(len(rssi0)):
            if rssi0[i] == 0 and rssi1[i] == 0:
                ratios[broadcasters[i]] = 0
                continue
            if rssi0[i] == 0 or rssi1[i] == 0:
                ratios[broadcasters[i]] = -1
                continue
            ratios[broadcasters[i]] = rssi0[i]/rssi1[i]
        ratios = dict(sorted(ratios.items(), key=lambda item: item[1]))
        for i in range(len(ratios)):
            values = list(ratios.values())
            keys = list(ratios.keys())
            if values[i] == 0:
                suspects[broadcasters[i]] = ""
            if i+1 == len(ratios):
                break
            if math.fabs(values[i] - values[i+1]) < 0.00001:

                suspects[keys[i]] = ""
                suspects[keys[i+1]] = ""
        for id, _ in suspects.items():
            self.score_list[id] += 1
            false_negative(node0, node1, id)
        if len(suspects) > 0:
            self._add_record(node0, node1, suspects)
            self._add_record(node1, node0, suspects)

    # thread task: hunt sybil nodes
    def hunt(self):
        # TODO: definitive sybil list
        definite_s = []
        while True:
            suspect_list = []
            # find the node with the highest score
            highest_list = []
            if not self._get_highest_list(highest_list):
                break
            if len(highest_list) > 1:
                for node in highest_list:
                    self._select_sybil_suspect(node, definite_s, suspect_list)
            node = None
            if len(suspect_list) == 0:
                node = highest_list[0]
            else:
                node = suspect_list[0]
            self.whitewash_and_punish(node, definite_s)
        print("Detection results:", self.normal_list, self.score_list, len(self.normal_list))

    def whitewash_and_punish(self, node, definite_s):
        for company_id in self.sentry_record[node]:  # go through to find all scores ought to be subtracted
            company_dict = self.sentry_record[node][company_id]
            score_sum = 0
            for node_id, node_score in company_dict.items():
                if node_id in definite_s:
                    continue
                score_sum = score_sum + node_score
                self.score_list[node_id] = self.score_list[node_id] - node_score
            if self.score_list[company_id] >= 0:
                self.score_list[company_id] = self.score_list[company_id] + score_sum
            self.sentry_record[company_id].pop(node)
        self.sentry_record[node].clear()
        self.score_list[node] = -1
        self.normal_list.remove(node)
        if node not in definite_s:
            definite_s.append(node)

    def _select_sybil_suspect(self, node, definite_s, suspect_list):
        for company_id in self.sentry_record[node]:  # if a node with the highest score fail to suspect a Sybil, it's eliminated
            company_dict = self.sentry_record[node][company_id]
            score_sum = 0
            for node_id in definite_s:
                if node_id in company_dict:
                    return True
        else:
            suspect_list.append(node)
        return False

    def _get_highest_list(self, highest_list):
        max_point = 0
        for i in range(len(self.score_list)):
            if self.score_list[i] > max_point:
                max_point = self.score_list[i]
        if max_point <= 0:
            return False
        for i in range(len(self.score_list)):
            if self.score_list[i] == max_point:
                highest_list.append(i)
        return True

    def _add_record(self, node0, node1, suspects: dict):
        temp_dict = None
        if node0 not in self.sentry_record:
            self.sentry_record[node0] = {}
        temp_dict = self.sentry_record[node0]
        if node1 in temp_dict:
            temp_dict = temp_dict[node1]
        else:
            temp_dict[node1] = {}
            temp_dict = temp_dict[node1]
        for score_id, _ in suspects.items():
            if score_id in temp_dict:
                temp_dict[score_id] = temp_dict[score_id] + 1
            else:
                temp_dict[score_id] = 1

    def _add_task(self, func, *args, **kwargs):
        task = Task(func, args=args, kwargs=kwargs)
        self.msg_queue.push(task)

    def add_score(self):
        listeners = self.listeners.copy()
        while len(listeners) > 1:
            id0 = listeners[0]
            id1 = listeners[1]
            listeners.remove(id0)
            listeners.remove(id1)
            self._add_task(self.suspect, id0, id1,
                           self.rssi_list[id0], self.rssi_list[id1], self.listeners.copy(), self.broadcasters.copy())

    def collect(self, node_id, rssi: dict):
        self.rssi_list[node_id] = list(rssi.values())
        if len(self.rssi_list) == len(self.listeners):
            self.add_score()
            self.rssi_list.clear()
            self.listeners.clear()

    def begin_round(self):
        if len(self.listeners) == 0:
            self.broadcasters.clear()
            for i in range(len(self.rounds)):
                if self.rounds[i][self.cur_rnd] == 0:
                    self.listeners.append(i)
                else:
                    self.broadcasters.append(i)
        self.cur_rnd += 1
        return True

    def process_finished(self):
        self._add_task(self._process_finished_task)
        self._add_task(self.hunt)

    def reset(self):
        self.score_list.clear()
        self.normal_list = list(range(self.node_num))
        self.rounds = np.zeros((self.node_num, self.cur_rnd), dtype=int).tolist()
        self.rssi_list = {}
        self.cur_rnd = 0
        self.listeners.clear()
        self.broadcasters.clear()
        self.sentry_record.clear()

    @property
    def broadcast_node_list(self):
        if len(self.broadcasters) == 0:
            self.begin_round()
        return self.broadcasters

    @property
    def listen_node_list(self):
        if len(self.broadcasters) == 0:
            self.begin_round()
        return self.listeners

    @property
    def total_rnds(self):
        return self.rnd


class Queue:
    def __init__(self):
        self._index = 0
        self.queue = []
        self.lock = threading.RLock()

    def push(self, task):
        self.lock.acquire()
        try:
            self.queue.append(task)
            self._index += 1
        finally:
            self.lock.release()

    def pop(self):
        self.lock.acquire()
        try:
            if self._index == 0:
                return
            self._index -= 1
        finally:
            self.lock.release()
        return self.queue.pop(0)

    @property
    def empty(self):
        return len(self.queue) == 0


class Task:
    def __init__(self, func, callback=None, args=(), kwargs={}):
        self.function = func
        self.callback = callback
        self.args = args
        self.kwargs = kwargs

    def run(self):
        try:
            if self.callback:
                result = self.callback(self, self.function(*self.args, **self.kwargs))
            else:
                result = self.function(*self.args, **self.kwargs)
            return result
        except Exception as e:
            if self.callback:
                result = self.callback(self, e)
            else:
                result = e
            return result