from main import db
from sqlalchemy import and_


def insert(obj):
    db.session.add(obj)
    db.session.commit()


def get_csr(username, code):
    return Csr.query.filter(and_(Csr.username == username, Csr.code == code)).all()


def delete(obj):
    db.session.delete(obj)
    db.session.commit()


class Cert(db.Model):
    __tablename__ = 'certificate'
    _id = db.Column(db.INT, primary_key=True)
    username = db.Column(db.String)
    timeMillis = db.Column(db.BigInteger)
    encodedCert = db.Column(db.String(2048))


class Csr(db.Model):
    __tablename__ = 'csr'
    _id = db.Column(db.INT, primary_key=True)
    username = db.Column(db.String)
    timeMillis = db.Column(db.BigInteger)
    encodedCsr = db.Column(db.String(1024))
    code = db.Column(db.String(8))
