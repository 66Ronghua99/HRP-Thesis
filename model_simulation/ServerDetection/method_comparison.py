class DetectMethod:
    def __init__(self, score_list, sentry_record, normal_list):
        self.score_list = score_list
        self.sentry_record = sentry_record
        self.normal_list = normal_list


class ThresholdOnly(DetectMethod):
    def calculate_threshold(self):
        pass


class PreviousEviction(ThresholdOnly):
    def hunt(self):
        pass


class NewEviction(ThresholdOnly):
    def hunt(self):
        pass
