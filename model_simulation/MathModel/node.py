import random


class Node(object):
    def __init__(self):
        self.nodelist = []
        self.score_dict = {}
        pass

    def is_this_type(self, id):
        if id in self.nodelist:
            return True
        return False

    def add_node(self, node_id):
        self.nodelist.append(node_id)
        if node_id not in self.score_dict:
            self.score_dict[node_id] = {}

    def add_score(self, node_id, company_id, score_id):
        temp_dict = self.score_dict[node_id]
        if company_id in temp_dict:
            temp_dict = temp_dict[company_id]
        else:
            temp_dict[company_id] = {}
            temp_dict = temp_dict[company_id]
        if score_id in temp_dict:
            temp_dict[score_id] = temp_dict[score_id] + 1
        else:
            temp_dict[score_id] = 1

    def clear(self):
        self.nodelist.clear()
        self.score_dict.clear()


class Sybil(Node):
    def __init__(self, count):
        super().__init__()
        self.count = count
        self.malicious = None

    def select_malicious(self):
        malicious = self.nodelist[0:self.count]
        self.nodelist = self.nodelist[self.count:]
        self.malicious = Malicious(malicious)

    def select_victim(self, senders):
        nodes = []
        for node in senders:
            if node in self.nodelist:
                nodes.append(node)
        if len(nodes) == len(self.nodelist) + len(self.malicious.nodelist):
            return []
        vacant = self.malicious.malicious_condition(senders)
        if len(nodes) - 1 > vacant:
            return random.sample(nodes, len(nodes) - vacant)
        return []


class Malicious(Node):
    def __init__(self, list):
        super().__init__()
        self.nodelist = list

    def malicious_condition(self, senders):
        vacant = 0
        for node in self.nodelist:
            if node in senders:
                vacant = vacant + 1
        return vacant


class Score(object):
    def __init__(self, id_1, id_2):
        self.id_1 = id_1
        self.id_2 = id_2
        pass
