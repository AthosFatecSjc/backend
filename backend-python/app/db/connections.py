import psycopg2
from pymongo import MongoClient

from app.core.config import MONGO_CONFIG, POSTGRES_CONFIG


def get_postgres_connection():
    return psycopg2.connect(**POSTGRES_CONFIG)


def get_mongo_client():
    return MongoClient(**MONGO_CONFIG)
