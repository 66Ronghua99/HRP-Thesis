on_terminal = True
to_file = False
log_file = None


def log(*args, file=None):
    if on_terminal:
        print(*args)
    if to_file or file:
        if not log_file:
            init_file(file)
        string = ""
        for strr in list(args):
            string += str(strr)
            string += " "
        string += "\n"
        log_file.write(string)
        pass


def init_file(file):
    global log_file
    if file:
        log_file = open(file, "w+")
        return
    log_file = open("log_file.txt", "w+")


def close_file():
    log_file.close()
