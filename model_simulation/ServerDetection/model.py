import random
from ServerDetection.server import Server
from ServerDetection.node import Node
import numpy as np


class Model:
    def __init__(self, node_num, sybil_percent):
        self.server = Server(node_num)
        self.counts = node_num
        self.nodes = {}
        self.normals = list(range(node_num))
        self.maps = Maps(20, 20)
        self.maps.init_loc(node_num)
        # self.maps.print_map()
        self.sybils = random.sample(range(self.counts), int(sybil_percent * self.counts))
        for id in self.sybils:
            self.normals.remove(id)
        self.init_nodes()
        pass

    def init_nodes(self):
        for id in range(self.counts):
            if id in self.normals:
                self.nodes[id] = Node(1, id, self.maps.get_node_loc(id))
            else:
                self.nodes[id] = Node(1, id, self.maps.get_node_loc(id), [0, 0], is_evil=True)

    def main_process(self):
        # broadcasting and receiving process
        print("Sybils:", self.sybils, "Normals:", self.normals)
        for rnd in range(self.server.total_rnds):
            self.server.begin_round()
            broadcasters = self.server.broadcast_node_list
            listeners = self.server.listen_node_list
            locations = None
            signal_strength = None
            locations, signal_strength = self._init_broadcasters(broadcasters)
            for r in listeners:
                node: Node = self.nodes[r]
                if not node.is_evil:
                    self._receiver_behavior(node, locations, signal_strength, broadcasters)
                else:
                    # Sybil receiver behavior
                    self._sybil_receiver_behavior(node, locations, signal_strength, broadcasters)
        self.server.process_finished()
        print("Main process exit")

    def _init_broadcasters(self, broadcasters):
        locations = self._b_locations(broadcasters)
        signal_strength = self._b_signal_strength(broadcasters)
        return locations, signal_strength

    def _b_signal_strength(self, broadcasters):
        signal_strength = {}
        for id in broadcasters:
            node: Node = self.nodes[id]
            if node.is_evil:
                # Sybil specific behavior can be added, choose not to broadcast
                signal_strength[id] = node.get_b_s_s()
            else:
                signal_strength[id] = node.get_b_s_s()
        return signal_strength

    def _b_locations(self, broadcasters):
        locations = {}
        for id in broadcasters:
            node: Node = self.nodes[id]
            locations[id] = node.get_loc()  # Later Sybil may use some other malicious devices to broadcast, loc changed
        return locations

    def _sybil_b_behavior(self, broadcasters):
        pass

    def _normal_b_behavior(self):
        pass

    def _receiver_behavior(self, node, locations, signal_strength, broadcasters):
        for b in broadcasters:
            node.set_rssi(b, locations[b], signal_strength[b])
        node.report(self.server)

    def _sybil_receiver_behavior(self, node, locations, signal_strength, broadcasters):
        for b in broadcasters:
            if b in self.normals:
                node.set_rssi(b, [21, 21], 1)
            else:
                sybil: Node = self.nodes[b]
                node.set_rssi(b, sybil.gps_loc, 1)
        node.report(self.server)


class Maps:
    def __init__(self, length: int, height: int):
        self.maps = np.zeros((length+1, height+1), dtype=int).tolist()
        self.middle_x = int(length/2)
        self.middle_y = int(height/2)
        self.x_max = int(length/2) if length%2==0 else int(length/2 + 1)
        self.x_min = -int(length/2)
        self.y_max = int(height/2) if height%2==0 else int(height/2 + 1)
        self.y_min = -int(height/2)
        self.loc_map = {}

    def init_loc(self, node_num: int):
        for id in range(node_num):
            self.random_loc(id)

    def set_node(self, x, y, id):
        if x>self.x_max or x<self.x_min or y>self.y_max or y<self.y_min:
            return False
        if x==0 and y==0:
            return False
        if self.maps[self.middle_y + y][self.middle_y + y] == 0:
            self.maps[self.middle_y + y][self.middle_x + x] = 1
            self.loc_map[id] = [self.middle_y + y, self.middle_y + y]
            return True
        return False

    def get_node_loc(self, id):
        if id in self.loc_map:
            return self.loc_map[id]
        return None

    def random_loc(self, id):
        x, y = 0, 0
        while True:
            x = random.randint(self.x_min, self.x_max+1)
            y = random.randint(self.y_min, self.y_max+1)
            if self.set_node(x, y, id):
                break

    def print_map(self):
        for i in range(len(self.maps)):
            print(self.maps[i])
