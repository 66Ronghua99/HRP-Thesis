import math
import threading
import numpy as np
from ServerDetection.utils import iteration
import time


class Server(object):
    def __init__(self, num=16):
        self.node_num = num
        self.rnd = 2 * int(math.log(num, 2))
        self.score_list = np.zeros(self.node_num, dtype=int).tolist()
        self.rounds = np.zeros((self.node_num, self.rnd), dtype=int).tolist()
        self.normal_list = []
        self.rssi_list = {}
        self.cur_rnd = 0
        self.listeners = []
        self.broadcasters = []
        self.sentry_record = {}
        self.threads = []
        self.msg_queue = Queue()
        self.init_thread()
        iteration(self.rounds, 0, 0, self.node_num)
        pass

    def thread_loop_task(self):
        while True:
            while not self.msg_queue.empty:
                task: Task = self.msg_queue.pop()
                task.run()
                pass
            time.sleep(0.5)

    def init_thread(self):
        td = threading.Thread(target=self.thread_loop_task)
        self.threads.append(td)
        td.start()

    def _process_finished_task(self):
        print("process finished! Score list:", self.score_list)

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
        sorted(ratios, key=lambda x: ratios[x])
        for i in range(len(ratios)):
            values = list(ratios.values())
            keys = list(ratios.keys())
            if values[i] == 0:
                suspects[broadcasters[i]] = ""
            if i+1 == len(ratios):
                break
            if math.fabs(values[i] - values[i+1]) < 0.001:
                suspects[keys[i]] = ""
                suspects[keys[i+1]] = ""
        for id, _ in suspects.items():
            self.score_list[id] += 1
        if len(suspects) > 0:
            self._add_record(node0, node1, suspects)
            self._add_record(node1, node0, suspects)
        print("Cur score list:", self.score_list)

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
        for i in range(0, len(self.listeners), 2):
            if i+1 == len(self.listeners):
                break
            id0 = self.listeners[i]
            id1 = self.listeners[i+1]
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

    def reset(self):
        self.score_list.clear()
        self.normal_list.np.zeros(self.node_num, dtype=int).tolist()
        self.rounds = np.zeros((self.counts, self.cur_rnd), dtype=int).tolist()
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

    def push(self, task):
        self.queue.append(task)
        self._index += 1

    def pop(self):
        if self._index == 0:
            return
        self._index -= 1
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