from ServerDetection.server import Server


class Server2(Server):

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

