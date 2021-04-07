from ServerDetection.server import Server
from ServerDetection.utils import euclidean_d

if __name__ == '__main__':
    server = Server(8)
    rssi = {4: 1, 5: 1, 6: 99, 7: 43}
    rssi2 = {4: 0, 5: 1, 6: 9, 7: 23}
    rssi3 = {4: 1, 5: 1, 6: 59, 7: 31}
    rssi4 = {4: 1, 5: 1, 6: 13, 7: 7}
    server.collect(0, rssi)
    server.collect(1, rssi2)
    server.collect(2, rssi3)
    server.collect(3, rssi4)

if __name__ == '__main__':
    print(euclidean_d([1, 2], [3, 4]))
    exit(0)

if __name__ == '__main__':
    pass


