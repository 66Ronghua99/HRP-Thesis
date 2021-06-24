import math
from queue import Queue
import threading
import time
import random

import OpenSSL.crypto
import numpy as np
from services.ca_service import get_pub_key


# TODO: 把整个server搬移过来，然后配置好客户端的连接，管理好线程
class NodeServer:
    chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    wait_response = {
        "role": 0,
        "random_code": None,
        "id": 0
    }

    verify_response = {
        "result": True
    }

    def __init__(self):
        # This node num is not fixed in a real system
        self.node_num = 2
        self.rnd = 2 * int(math.log(self.node_num, 2))
        self.score_list = np.zeros(self.node_num, dtype=int).tolist()
        self.rounds = None
        self.POLL_QUEUE_DICT = {}
        self.CLIENT_DICT = {}
        self.RANDOM_CODE_DICT = {}
        self.PUBLIC_KEY_DICT = {}
        self.POLL_LIST = []
        self.id = 0
        self.lock = threading.RLock()
        self.threads = []
        self.task_list = Queue()
        self.rssi_dict = {}
        self.cur_rnd = 0
        self.listeners = []
        self.broadcasters = []
        self.sentry_record = {}
        self._init_task_consumer()

    # wait for all nodes to register
    def register(self, request_data):
        uid = request_data["username"]
        pub_key = request_data["publicKey"]
        self.lock.acquire()
        try:
            # TODO: protect the interface from visiting randomly
            self.CLIENT_DICT[uid] = self.id
            if uid not in self.POLL_QUEUE_DICT and self.CLIENT_DICT[uid] not in self.PUBLIC_KEY_DICT:
                self.POLL_QUEUE_DICT[uid] = Queue()
                self.PUBLIC_KEY_DICT[self.CLIENT_DICT[uid]] = pub_key
                self.POLL_LIST.append(uid)
                self.id += 1
            if self.id == self.node_num:
                self._add_task(self._start_polling_thread, self._wait_polling)
        finally:
            self.lock.release()
        return self.get_response(uid)

    def get_response(self, uid):
        q = self.POLL_QUEUE_DICT[uid]
        try:
            wait_response = q.get()
        except q.Empty:
            wait_response = ""
        return wait_response

    def _get_random_code(self):
        rnd_code = ""
        for i in range(4):
            a = random.randint(0, 35)
            rnd_code += NodeServer.chars[a]
        return rnd_code

    # verify the random code
    # { "id": xxx, "random_code": xxxx }
    def verify_random_code(self, request_data):
        id = int(request_data["id"])
        rand_code = request_data["random_code"].upper()
        if rand_code == self.RANDOM_CODE_DICT[id]:
            return NodeServer.verify_response
        return {"result": False}

    # detector saying that they are ready
    def detector_ready(self, request_data):
        uid = request_data["username"]
        id = self.CLIENT_DICT[uid]
        self.rssi_dict[id] = request_data["rssi"]
        if id not in self.listeners:
            return {"error": "you are not a detector this round"}
        self.lock.acquire()
        try:
            self.POLL_LIST.append(uid)
            if len(self.POLL_LIST) == self.node_num:
                self._add_task(self._start_polling_thread, self._round_polling)
        finally:
            self.lock.release()
        return self.get_response(uid)

    def broadcaster_ready(self, request_data):
        uid = request_data["username"]
        id = self.CLIENT_DICT[uid]
        if id not in self.broadcasters:
            return {"error": "you are not a broadcaster this round"}
        self.lock.acquire()
        try:
            self.POLL_LIST.append(uid)
            if len(self.POLL_LIST) == self.node_num:
                self._add_task(self._start_polling_thread, self._round_polling)
        finally:
            self.lock.release()
        return self.get_response(uid)

    def _start_polling_thread(self, *args):
        threading.Thread(target=args[0]).start()

    def _wait_polling(self):
        self.lock.acquire()
        try:
            if not self.rounds:
                self.init_rounds()
            self._set_b_and_l()
            for uid in self.POLL_LIST:
                id = self.CLIENT_DICT[uid]
                q = self.POLL_QUEUE_DICT[uid]
                if id not in self.listeners:
                    continue
                response = NodeServer.wait_response.copy()
                response["random_code"] = self._get_random_code()
                response["id"] = id
                self.RANDOM_CODE_DICT[id] = response["random_code"]
                q.put(response)
            # pretend the detectors can always start scanning first
            time.sleep(0.5)
            for uid in self.POLL_LIST:
                id = self.CLIENT_DICT[uid]
                q = self.POLL_QUEUE_DICT[uid]
                if id not in self.broadcasters:
                    continue
                response = NodeServer.wait_response.copy()
                response["random_code"] = self._get_random_code()
                response["role"] = 1
                response["id"] = id
                self.RANDOM_CODE_DICT[id] = response["random_code"]
                q.put(response)
            self.POLL_LIST.clear()
            time.sleep(0.5)
        finally:
            self.lock.release()

    def _round_polling(self):
        self.lock.acquire()
        try:
            self._add_task(self._add_score)
            if self.rnd == self.cur_rnd:
                response = {"role": -1, "random_code": "000000", "id": "-1"}
                for uid in self.POLL_LIST:
                    self.POLL_QUEUE_DICT[uid].put(response)
                self.POLL_LIST.clear()
                self.id = 0
                return
            self._set_b_and_l()
            for uid in self.POLL_LIST:
                id = self.CLIENT_DICT[uid]
                q = self.POLL_QUEUE_DICT[uid]
                if id not in self.listeners:
                    continue
                response = NodeServer.wait_response.copy()
                response["random_code"] = self._get_random_code()
                self.RANDOM_CODE_DICT[id] = response["random_code"]
                q.put(response)
            # pretend the detectors can always start scanning first
            time.sleep(0.5)
            for uid in self.POLL_LIST:
                id = self.CLIENT_DICT[uid]
                q = self.POLL_QUEUE_DICT[uid]
                if id not in self.broadcasters:
                    continue
                response = NodeServer.wait_response.copy()
                response["random_code"] = self._get_random_code()
                response["role"] = 1
                self.RANDOM_CODE_DICT[self.CLIENT_DICT[uid]] = response["random_code"]
                q.put(response)
            time.sleep(0.5)
            self.POLL_LIST.clear()
            self.id = 0
        finally:
            self.lock.release()

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
            self._add_task(self._suspect, id0, id1,
                           self.rssi_dict[id0], self.rssi_dict[id1], listeners, broadcasters)
            if ptr1 + 1 == len(listeners):
                break

    # thread adding score task server detection logic ##########################
    def _suspect(self, node0, node1, rssi0, rssi1, listeners, broadcasters):
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
        if len(suspects) > 0:
            self._add_record(node0, node1, suspects)
            self._add_record(node1, node0, suspects)

    # thread task: hunt sybil nodes
    def _hunt(self):
        # TODO: definitive sybil list
        definite_s = []
        while True:
            self._select_definite_sybils(definite_s)
            suspect_list = []
            # find the node with the highest score
            highest_list = []
            if not self._get_highest_list(highest_list):
                break
            if len(highest_list) > 1:
                for node in highest_list:
                    self._select_sybil_suspect(node, definite_s, suspect_list)
            eliminate_list = None
            if len(suspect_list) == 0:
                eliminate_list = highest_list
            else:
                eliminate_list = suspect_list
            for node in eliminate_list:
                self._whitewash_and_punish(node, definite_s)

    def _whitewash_and_punish(self, node, definite_s):
        is_all_score_from_definite_sybils = True
        has_suspect = False
        for company_id in self.sentry_record[node]:  # go through to find all scores ought to be subtracted
            has_suspect = True
            company_dict = self.sentry_record[node][company_id]
            score_sum = 0
            for node_id, node_score in company_dict.items():
                if node_id in definite_s:
                    continue
                is_all_score_from_definite_sybils = False
                score_sum = score_sum + node_score
                self.score_list[node_id] = self.score_list[node_id] - node_score
            if self.score_list[company_id] >= 0:
                self.score_list[company_id] = self.score_list[company_id] + score_sum
            self.sentry_record[company_id].pop(node)
        self.sentry_record[node].clear()
        self.score_list[node] = -1
        # The node votes for some other, it's not in definite_s and the node votes for nodes in definite_s
        if is_all_score_from_definite_sybils and node not in definite_s and has_suspect:
            return False
        self.normal_list.remove(node)
        # if node not in definite_s:
        #     definite_s.append(node)
        return True

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

    def _select_definite_sybils(self, d_s):
        score_list = {}
        for id in range(len(self.score_list)):
            if self.score_list[id] < 0:
                continue
            score_list[id] = self.score_list[id]
        std = np.std(list(score_list.values()))
        mean = np.mean(list(score_list.values()))
        threshold = mean + 1.7 * std
        for id, score in score_list.items():
            if score > threshold and id not in d_s:
                d_s.append(id)

    def _get_highest_list(self, highest_list):
        max_point = 0
        for i in range(len(self.score_list)):
            if self.score_list[i] > max_point:
                max_point = self.score_list[i]
        if max_point <= self.threshold:
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
    ################################################################

    def _add_task(self, func, *args, **kwargs):
        task = Task(func, args=args, kwargs=kwargs)
        self.task_list.put(task)

    def _task_comsumer(self):
        while True:
            task = self.task_list.get()
            task.run()

    def _init_task_consumer(self):
        thread = threading.Thread(target=self._task_comsumer)
        thread.start()
        self.threads.append(thread)

    def init_rounds(self):
        self.rounds = np.zeros((self.node_num, self.rnd), dtype=int).tolist()
        iteration(self.rounds, 0, 0, self.node_num)

    def _set_b_and_l(self):
        self.listeners.clear()
        self.broadcasters.clear()
        for i in range(len(self.rounds)):
            if self.rounds[i][self.cur_rnd] == 0:
                self.listeners.append(i)
            else:
                self.broadcasters.append(i)
        self.cur_rnd += 1


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