on_terminal = False
to_file = True
log_file = None


def log(*args):
    if on_terminal:
        print(*args)
    if to_file:
        if not log_file:
            init_file()
        string = ""
        for strr in list(args):
            string += str(strr)
            string += " "
        string += "\n"
        log_file.write(string)
        pass


def init_file():
    global log_file
    log_file = open("log_file.txt", "w+")


def close_file():
    global log_file
    log_file.close()
