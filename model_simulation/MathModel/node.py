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

    def add_score(self, node_id, score_id):
        dict = self.score_dict[node_id]
        if score_id in dict:
            dict[score_id] = dict[score_id] + 1
        else:
            dict[score_id] = 1

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
        self.nodelist = self.nodelist[1:]
        self.malicious = Malicious(malicious)

    def check_ap(self, graph, round):
        vacant = self.malicious.malicious_condition(graph, round)
        if vacant > 0:
            for i in range(vacant):
                pass #random choose sybils to broadcast


class Malicious(Node):
    def __init__(self, list):
        super().__init__()
        self.nodelist = list

    def malicious_condition(self, graph, round):
        vacant = 0
        for node in self.nodelist:
            if graph[node][round] == 0:
                vacant = vacant + 1
        return vacant
