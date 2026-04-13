import os
from typing import List

from dotenv import load_dotenv

load_dotenv()


def get_origins() -> List[str]:
    origins = os.getenv("CORS_ORIGINS", "http://localhost:3000")
    return [origin.strip() for origin in origins.split(",") if origin.strip()]


POSTGRES_CONFIG = {
    "host": os.getenv("POSTGRES_HOST", "localhost"),
    "port": int(os.getenv("POSTGRES_PORT", "55432")),
    "database": os.getenv("POSTGRES_DB", "energia_db"),
    "user": os.getenv("POSTGRES_USER", ""),
    "password": os.getenv("POSTGRES_PASSWORD", ""),
}

MONGO_CONFIG = {
    "host": os.getenv("MONGO_HOST", "localhost"),
    "port": int(os.getenv("MONGO_PORT", "27017")),
    "username": os.getenv("MONGO_USERNAME", ""),
    "password": os.getenv("MONGO_PASSWORD", ""),
    "authSource": os.getenv("MONGO_AUTH_SOURCE", "admin"),
}

MONGO_DATABASE = os.getenv("MONGO_DATABASE", "energia_analytics")
PORT = int(os.getenv("PORT", "8000"))

INTERNAL_LOG_URL = os.getenv(
    "INTERNAL_LOG_URL",
    "http://backend-java:8181/internal/jobs/aneel/logs",
)
INTERNAL_LOG_API_KEY = os.getenv("INTERNAL_LOG_API_KEY", "")
