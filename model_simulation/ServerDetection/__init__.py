from ServerDetection.model import Model

if __name__ == '__main__':
    for i in range(2000):
        model = Model(16, 0.4)
        model.main_process()

