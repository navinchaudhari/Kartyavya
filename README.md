# Kartyavya (CivicFix)

AI-powered civic issue reporting and resolution platform. Team 2, C-DAC. Project Lead: Navin Anil Chaudhari.

## Seven applications
config-server (8888, M1) - service-registry (8761, M5) - api-gateway (8080, M6) - access-admin-service (8081, M1)
civic-report-service (8082, M2) - ai-analytics-service (8083, M3) - email-notification-service (8084, M4)
frontend (5173, M5 + M6)

## Local run order
```
docker compose -f infra/docker-compose.yml up --build
```
or manually, in this order: MySQL/Mongo/RabbitMQ -> config-server -> service-registry -> the four business
services + api-gateway -> frontend (`cd frontend && npm ci && npm run dev`).

## Repositories
- `kartyavya` (this repo) - all source code, contracts, CI.
- `kartyavya-config` (separate private repo) - non-secret Spring Cloud Config profiles only. Never commit secrets.

## Contributing
See `.github/pull_request_template.md` and `docs/contracts/member-ownership.md` for the branch/PR/review rules.
