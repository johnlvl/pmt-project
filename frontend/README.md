# PMT Frontend (Angular)

Dev: npm start (port 4200)
Build: npm run build (dist/pmt-frontend)
Test: npm test ; coverage: npm run coverage (coverage/)

Runtime API base URL is injected via window.RUNTIME_CONFIG.API_BASE_URL.
Docker image serves static files with Nginx and sets API_BASE_URL from env.

## Quick start

- Install Node 20+
- Install deps: npm ci
- Run dev server:

```powershell
npm start
```

## Docker

Build and run:

```powershell
docker build -t pmt-frontend:dev .
docker run -p 4200:80 -e API_BASE_URL=http://localhost:8080 pmt-frontend:dev
```

## With docker-compose

From repo root (backend directory):

```powershell
docker compose up --build
```

This starts MySQL (3306), backend (8080), and frontend (4200).