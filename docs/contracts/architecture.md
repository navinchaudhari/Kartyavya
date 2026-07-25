# Kartyavya - Frozen Architecture

Status: FROZEN (v1.0). Any change requires a contract-change PR (see change-control in member-ownership.md).

## Stack
- Backend: Java 21, Spring Boot 3.5.16, Spring Cloud 2025.0.3 BOM
- Frontend: React 19 (Vite)
- Databases: MySQL 8.0 (access_db, report_db), MongoDB (ai_analytics_db, notification_db)
- Messaging: RabbitMQ topic exchange `kartyavya.events`
- Discovery: Netflix Eureka
- Config: Spring Cloud Config Server (private repo `kartyavya-config`)

## Seven deployable applications
1. config-server (8888) - M1
2. service-registry / Eureka (8761) - M5
3. api-gateway (8080) - M6
4. access-admin-service (8081) - M1
5. civic-report-service (8082) - M2
6. ai-analytics-service (8083) - M3
7. email-notification-service (8084) - M4
8. frontend (5173 local) - M5 (public/citizen/auth/map) + M6 (officer/admin)

## Communication rules
- All user traffic enters through API Gateway only. No Nginx. React never calls a business-service port directly.
- Internal synchronous calls: OpenFeign + Eureka + LoadBalancer, limited to two approved routes (see feign-contracts.md).
- Asynchronous domain events: RabbitMQ topic exchange (see rabbitmq-events.md). Report submission/status changes never block on AI or Email.
- No REST call from Report Service to AI or Email Service. No direct database access across services. No shared JPA entities.

## Startup order (always, every integration session)
1. MySQL, MongoDB, RabbitMQ
2. Config Server (8888) - verify /actuator/health
3. Eureka Server (8761) - open registry dashboard
4. Access, Civic Report, AI & Analytics, Email Notification - confirm UP in Eureka
5. API Gateway (8080) - verify every lb:// route resolves
6. React frontend - test only through http://localhost:8080/api/...

## Non-negotiable rules
- R-01 Project name is always written as Kartyavya.
- R-02 Java 21 / Spring Boot 3.5.16 / React 19 / Spring Cloud 2025.0.3 are frozen.
- R-03 No service directly reads or joins another service's database.
- R-04 MySQL schemas only via Flyway; Hibernate ddl-auto = validate (never update/create-drop).
- R-05 No JPA entity is shared between services - only explicit DTOs.
- R-06 All user traffic enters through API Gateway in integrated/production environments.
- R-07 OpenFeign only for the two approved immediate-response lookups.
- R-08 AI, analytics and email processing use RabbitMQ and never block report submission.
- R-09 All HTTP errors use one canonical JSON structure.
- R-10 No API path, JSON field, enum, service ID, routing key or DB column changes without a contract-change PR.
- R-11 Only email notification is in scope; SMS is out of scope.
- R-12 Secrets are env vars / secret-store values, never committed to either repo.
