import random

all_broadcast = 0


class Node(object):
    score_dict = {}

    def __init__(self):
        self.nodelist = [] # id of nodes
        pass

    def is_this_type(self, id):
        if id in self.nodelist:
            return True
        return False

    def add_node(self, node_id):
        self.nodelist.append(node_id)
        if node_id not in Node.score_dict:
            Node.score_dict[node_id] = {}

    # When adding points to others, both nodes in the Sentry would add each other to score_dict. This is the record
    # for later elimination. We'll know who reports who. Add or subtract points from nodes at the stage of hunting.
    def add_score(self, node_id, company_id, score_id):
        temp_dict = Node.score_dict[node_id]
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
        Node.score_dict.clear()


class Sybil(Node):
    def __init__(self, count):
        super().__init__()
        self.count = count
        self.malicious = None

    # from Sybil nodes, some are selected as malicious nodes
    def select_malicious(self):
        malicious = self.nodelist[0:self.count]
        self.nodelist = self.nodelist[self.count:]
        self.malicious = Malicious(malicious)

    # randomly select victims when some sybils are going to be reported
    def select_victim(self, senders):
        global all_broadcast
        nodes = []
        for node in senders:
            if node in self.nodelist:
                nodes.append(node)
        vacant = self.malicious.malicious_condition(senders)
        if len(nodes) == len(self.nodelist) and vacant == 0:
            all_broadcast = all_broadcast + 1
            return random.sample(nodes, len(nodes) - 1)
        if len(nodes) - 1 > vacant:
            return random.sample(nodes, len(nodes) - vacant)
        return []


class Malicious(Node):
    def __init__(self, list):
        super().__init__()
        self.nodelist = list

    def malicious_condition(self, senders):
        vacant = len(self.nodelist)
        for node in self.nodelist:
            if node in senders:
                vacant = vacant - 1
        return vacant


# not in use
class Score(object):
    def __init__(self, id_1, id_2):
        self.id_1 = id_1
        self.id_2 = id_2
        pass
