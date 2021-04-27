from ServerDetection.server import Server


class Server2(Server):
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
            if ptr1 +1 == len(listeners):
                break
