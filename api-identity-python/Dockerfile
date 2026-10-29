FROM python:3.12-slim
WORKDIR /app

# Install the SDK first (local path dependency).
COPY sdk-identity-python /sdk-identity-python
RUN pip install --no-cache-dir /sdk-identity-python

COPY api-identity-python/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY api-identity-python/app ./app
EXPOSE 4102
CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port ${PORT:-4102}"]
