# Local orchestration (docker-compose)

Services:
- db: MySQL 8.4
- backend: Spring Boot (port 8080)
- frontend: Angular + Nginx (port 4200)

## Usage

```powershell
docker compose up --build
```

Then open http://localhost:4200
Backend API: http://localhost:8080

## Environment overrides

- MYSQL_ROOT_PASSWORD, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD
- API_BASE_URL for frontend
- SPRING_DATASOURCE_* for backend