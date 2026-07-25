# Config Server Contracts - Owner: M1

## Config repository layout (private repo `kartyavya-config`)
application.yml, eureka-server.yml, api-gateway.yml, access-admin-service.yml, civic-report-service.yml,
ai-analytics-service.yml, email-notification-service.yml, application-local.yml, application-docker.yml, application-prod.yml

## Client bootstrap contract (every service)
```yaml
spring:
  application:
    name: <service-name>
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```
- Every client sets spring.application.name locally before remote config loads.
- Config Server does not depend on Eureka to start.
- Secrets stay as ${ENV_VAR} placeholders in the config repo - never committed literal values.
- `optional:configserver:` is only for isolated local tests; integrated builds require Config Server running.

## Configuration key catalog (see Appendix B of the frozen contracts PDF for full table)
Common: SPRING_PROFILES_ACTIVE, CONFIG_SERVER_URL, EUREKA_URL
Security: JWT_SECRET, JWT_EXPIRY_MINUTES, INTERNAL_SERVICE_KEY
MySQL: MYSQL_HOST, MYSQL_PORT, MYSQL_USER, MYSQL_PASSWORD
MongoDB: MONGO_URI
RabbitMQ: RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USER, RABBITMQ_PASSWORD, RABBITMQ_EXCHANGE
Cloudinary: CLOUDINARY_URL
AI: AI_API_KEY (optional; fallback mock)
Email: SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, EMAIL_FROM
