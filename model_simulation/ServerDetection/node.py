import random
from ServerDetection.utils import euclidean_d
from ServerDetection.server import Server


class Node(object):
    def __init__(self, default_ss, id, location, ap_loc=None, is_evil=False):
        self.default_b_signal_strength = default_ss # Sybils default -1(not broadcast) normal: random diff num
        self.b_signal_strength = None
        self.rssi = {}
        self.id = id
        self.gps_loc = location
        self.ap_loc = ap_loc
        self.is_evil = is_evil
        self.set_b_s_s()
        pass

    # broadcasters need to set the b_s_s, listeners will retrieve these values,
    # times their coefficient and taken as RSSI
    def set_b_s_s(self, b_s_s=None):
        self.b_signal_strength = b_s_s if b_s_s else self.default_b_signal_strength

    def get_b_s_s(self):
        if not self.b_signal_strength:
            self.set_b_s_s()
        return self.b_signal_strength

    def set_rssi(self, node_id, loc, signal_strength):
        # generate real rssi value
        self.rssi[node_id] = float(euclidean_d(loc, self.gps_loc) * signal_strength)

    def forge_rssi(self, node_id):
        if not self.is_evil:
            return
        self.rssi[node_id] = 10101
        pass

    def get_loc(self):
        if self.is_evil:
            return self.ap_loc
        return self.gps_loc

    def report(self, server):
        result = server.collect(self.id, self.rssi)
        self.rssi.clear()
        return result
