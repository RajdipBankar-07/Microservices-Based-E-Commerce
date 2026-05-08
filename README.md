# Microservices-Based-E-Commerce
Microservices-Based E-Commerce Backend (Spring Boot).

## Day 27: Zipkin Tracing with Docker Compose

- `docker-compose.yml` now includes a `zipkin` service (`openzipkin/zipkin:3.4.0`).
- The app service exports traces to Zipkin with:
     - `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans`

### Run Stack

```powershell
cd ecommerce
docker compose up --build
```

### Verify

- API: `http://localhost:8080/actuator/health`
- Zipkin UI: `http://localhost:9411`

## Day 28: Prometheus + Grafana Monitoring

- Added Prometheus service (`prom/prometheus:v2.53.1`) to scrape:
     - `http://app:8080/actuator/prometheus`
- Added Grafana service (`grafana/grafana:11.1.0`) with pre-provisioned Prometheus datasource.

### Run Stack

```powershell
cd ecommerce
docker compose up --build
```

### Verify

- Prometheus UI: `http://localhost:9090`
- Grafana UI: `http://localhost:3001`
     - Username: `admin`
     - Password: `admin123`
- App metrics endpoint: `http://localhost:8080/actuator/prometheus`


<!--      git add .
     git commit -m "Database Connection"
     git pull origin main --rebase
     git push origin main          -->