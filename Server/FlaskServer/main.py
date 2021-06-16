from flask import Flask, request
from flask_sqlalchemy import SQLAlchemy
from controller import ctr
from flask_mail import Mail
app = Flask(__name__)



class Config(object):
    """配置参数"""
    # 设置连接数据库的URL
    user = 'root'
    password = 'root'
    database = 'caserver'
    app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://%s:%s@127.0.0.1:3306/%s' % (user, password, database)

    # 设置sqlalchemy自动更跟踪数据库
    SQLALCHEMY_TRACK_MODIFICATIONS = True

    # 查询时会显示原始SQL语句
    app.config['SQLALCHEMY_ECHO'] = True

    # 禁止自动提交数据处理
    app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN'] = False


app.config.from_object(Config)
app.config.update(dict( DEBUG=True,
                        MAIL_SERVER='smtp.126.com',
                        MAIL_PORT=25,
                        MAIL_USE_TLS=True,
                        MAIL_USERNAME="lironghua980303@126.com",
                        MAIL_PASSWORD="RTMYNXJVQWJNWOBL",
                        MAIL_DEFAULT_SENDER='CsrVerify <lironghua980303@126.com>')) #this is required
db = SQLAlchemy(app)
mail = Mail(app)

app.register_blueprint(ctr)


@app.route("/")
def index():
    return "please enter correct URL"


if __name__ == '__main__':
    app.run(debug=True, port=8080, threaded=True)
