# Config Server Contracts - Owner: M1

Format rule: all Spring configuration in this project uses `.properties` files, not YAML. This applies to the
`kartyavya-config` repository, every service's local `bootstrap.properties`, and Config Server's own
`application.properties`. (Docker Compose and GitHub Actions files stay YAML - that's a fixed requirement of
those tools, not a Spring config choice, so `infra/docker-compose.yml` and `.github/workflows/ci.yml` are
unaffected by this rule.)

## Config repository layout (private repo `kartyavya-config`)
application.properties, eureka-server.properties, api-gateway.properties, access-admin-service.properties,
civic-report-service.properties, ai-analytics-service.properties, email-notification-service.properties,
application-local.properties, application-docker.properties, application-prod.properties

## Client bootstrap contract (every service) - `src/main/resources/bootstrap.properties`
```properties
spring.application.name=<service-name>
spring.config.import=optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}
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
