# Service Catalog (Frozen)

| Service ID | Port | Owner | Responsibility | Persistence |
|---|---|---|---|---|
| config-server | 8888 | M1 | Spring Cloud Config Server | No domain DB |
| eureka-server | 8761 | M5 | Netflix Eureka registry | No DB |
| api-gateway | 8080 | M6 | Public routing, JWT, CORS, rate limit, correlation ID | No DB |
| access-admin-service | 8081 | M1 | Auth, users, roles, departments, routing rules | access_db / MySQL |
| civic-report-service | 8082 | M2 | Reports, status workflow, maps, duplicates, audit | report_db / MySQL |
| ai-analytics-service | 8083 | M3 | AI classification, corrections, analytics | ai_analytics_db / MongoDB |
| email-notification-service | 8084 | M4 | RabbitMQ email consumers, retries, delivery logs | notification_db / MongoDB |
| frontend | 5173 local | M5 (public/citizen/auth/map), M6 (officer/admin) | React 19 client | No DB |

## spring.application.name values (must match exactly)
config-server, eureka-server (registry, not a Eureka client itself), api-gateway, access-admin-service,
civic-report-service, ai-analytics-service, email-notification-service

## Eureka registration
All of api-gateway, access-admin-service, civic-report-service, ai-analytics-service and
email-notification-service register with Eureka and must show status UP before frontend integration testing.
