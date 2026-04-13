FROM python:3.12-slim
WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN apt-get update \
    && apt-get install -y --no-install-recommends cron \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
RUN playwright install --with-deps chromium

COPY app ./app
COPY cron/aneel_perdas /etc/cron.d/aneel_perdas
COPY scripts/start-cron.sh /usr/local/bin/start-cron.sh
RUN chmod 0644 /etc/cron.d/aneel_perdas \
    && chmod +x /usr/local/bin/start-cron.sh

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
