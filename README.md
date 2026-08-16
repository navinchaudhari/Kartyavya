# Kartyavya — Clean Local Setup

Kartyavya is a React 19 and Spring Boot microservices platform for municipal complaint reporting, AI classification, routing, analytics and email notifications.

This package is configured around one rule:

> Edit `Kartyavya/.env` for local URLs, ports, credentials and integration settings. Do not create operating-system environment variables.

The archive also contains a sibling `kartyavya-config` folder. Push those property files to the remote GitHub Config Repository once. After that, normal local configuration changes are made only in `Kartyavya/.env`.

## Package layout

```text
Kartyavya-Clean-Package/
├── Kartyavya/
│   ├── .env
│   ├── pom.xml
│   ├── config-server/
│   ├── service-registry/
│   ├── api-gateway/
│   ├── access-admin-service/
│   ├── civic-report-service/
│   ├── ai-analytics-service/
│   ├── email-notification-service/
│   ├── common-contracts/
│   ├── frontend/
│   └── database/SCHEMA.sql
└── kartyavya-config/
    ├── application.properties
    ├── service-registry.properties
    ├── api-gateway.properties
    ├── access-admin-service.properties
    ├── civic-report-service.properties
    ├── ai-analytics-service.properties
    └── email-notification-service.properties
```

## Corrections included in this version

- A single Gateway CORS filter reads `KARTYAVYA_CORS_ALLOWED_ORIGINS` directly from `.env`.
- The Gateway removes accidental downstream `Access-Control-*` headers before returning a response.
- Port `5173` is the browser origin; port `8080` is the API destination; service ports such as `8083` are never emitted as browser origins.
- Duplicate Gateway `globalcors` and `DedupeResponseHeader` configuration has been removed.
- CORS preflight `OPTIONS` requests never require JWT authentication.
- `/api/ai/status` is public for configuration diagnostics; real analytics endpoints remain protected.
- Client-supplied `X-User-Id`, `X-User-Role` and `X-User-Email` headers are removed and replaced only after JWT verification.
- Role authorities are normalized to `ADMIN`, `OFFICER` and `CITIZEN` in all protected services.
- Gemini transient `429` and `5xx` failures use bounded retry with exponential backoff.
- Gemini hotspot failure no longer breaks the complete Admin Dashboard. Core summary, overview and heatmap data continue loading, while the AI panel shows an explicit degraded status.
- Frontend Gemini requests use a separate longer timeout controlled from `.env`.
- Hibernate ORM owns table creation and updates. Flyway is not used.
- Browser `alert()` and `confirm()` calls are replaced by React Toastify notifications.

## 1. Prerequisites

Install and verify:

```powershell
java -version
mvn -version
node -v
npm -v
mongosh --version
rabbitmq-diagnostics listeners
```

Recommended local versions:

```text
Java: 21
Maven: 3.9+
Node.js: 22+
npm: 10+
MySQL: 8+
MongoDB: 7+
RabbitMQ: 4+
```

## 2. Push the included Config Repository files once

The Config Server uses only the remote Git repository configured by:

```dotenv
KARTYAVYA_CONFIG_REPO_URI=https://github.com/navinchaudhari/kartyavya-config
```

Copy the files from the archive's sibling `kartyavya-config` folder into your local clone of that GitHub repository. Replace old property files with the included versions.

Run inside the local `kartyavya-config` repository:

```powershell
git status
git add .
git commit -m "Synchronize Kartyavya runtime configuration"
git push origin main
```

The remote repository must contain these exact files at its root:

```text
application.properties
service-registry.properties
api-gateway.properties
access-admin-service.properties
civic-report-service.properties
ai-analytics-service.properties
email-notification-service.properties
```

Do not upload `.env`, passwords, JWT secrets, Gmail app passwords or Gemini API keys to GitHub.

For a public Config Repository, leave these blank in `.env`:

```dotenv
GITHUB_USERNAME=
GITHUB_TOKEN=
```

For a private repository, enter the GitHub username and a token that can read repository contents.

## 3. Edit only `Kartyavya/.env`

Open:

```text
Kartyavya/.env
```

### Required security values

Replace both placeholders with different strong random values. The JWT secret must be at least 64 characters.

```dotenv
KARTYAVYA_JWT_SECRET=replace_with_a_long_random_value
KARTYAVYA_INTERNAL_SERVICE_KEY=replace_with_a_different_long_random_value
```

All services must use the same `.env`; otherwise JWT and internal service authentication will fail.

### Browser and Gateway values

For the default Vite and Gateway ports, keep:

```dotenv
FRONTEND_HOST=localhost
FRONTEND_PORT=5173
API_GATEWAY_HOST=localhost
API_GATEWAY_PORT=8080

VITE_API_ORIGIN=http://${API_GATEWAY_HOST}:${API_GATEWAY_PORT}
KARTYAVYA_CORS_ALLOWED_ORIGINS=http://${FRONTEND_HOST}:${FRONTEND_PORT}
```

Never set the CORS value to `8080`, `8082`, `8083` or `8084`.

For multiple frontend origins, use a comma-separated value without trailing slashes:

```dotenv
KARTYAVYA_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

### MySQL

Set credentials that exist in MySQL:

```dotenv
KARTYAVYA_MYSQL_HOST=localhost
KARTYAVYA_MYSQL_PORT=3306
KARTYAVYA_MYSQL_USER=kartyavya
KARTYAVYA_MYSQL_PASSWORD=dac
KARTYAVYA_ACCESS_DB_NAME=access_db
KARTYAVYA_REPORT_DB_NAME=report_db
KARTYAVYA_HIBERNATE_DDL_AUTO=update
```

### Gemini

Enter a valid Google AI Studio API key:

```dotenv
KARTYAVYA_GEMINI_ENABLED=true
KARTYAVYA_GEMINI_API_KEY=your_real_api_key
KARTYAVYA_GEMINI_MODEL=gemini-3.1-flash-lite
```

Retry and timeout values are also controlled from `.env`:

```dotenv
KARTYAVYA_GEMINI_CONNECT_TIMEOUT_SECONDS=10
KARTYAVYA_GEMINI_READ_TIMEOUT_SECONDS=45
KARTYAVYA_GEMINI_MAX_ATTEMPTS=3
KARTYAVYA_GEMINI_RETRY_DELAY_MILLIS=750
VITE_AI_REQUEST_TIMEOUT_MS=150000
```

Do not add quotes or spaces around the API key.

### Gmail

Keep delivery disabled until a Gmail app password is entered:

```dotenv
KARTYAVYA_EMAIL_DELIVERY_ENABLED=false
KARTYAVYA_MAIL_USERNAME=your_account@gmail.com
KARTYAVYA_MAIL_FROM=your_account@gmail.com
KARTYAVYA_MAIL_APP_PASSWORD=
```

After entering a valid app password:

```dotenv
KARTYAVYA_EMAIL_DELIVERY_ENABLED=true
```

## 4. Prepare MySQL databases

Hibernate creates and updates tables, but the two databases and account must exist first.

Run as a MySQL administrator:

```sql
CREATE DATABASE IF NOT EXISTS access_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS report_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'kartyavya'@'localhost' IDENTIFIED BY 'dac';
ALTER USER 'kartyavya'@'localhost' IDENTIFIED BY 'dac';

GRANT ALL PRIVILEGES ON access_db.* TO 'kartyavya'@'localhost';
GRANT ALL PRIVILEGES ON report_db.* TO 'kartyavya'@'localhost';
FLUSH PRIVILEGES;
```

Change the SQL password and `.env` password together when using another password.

`database/SCHEMA.sql` is reference documentation only. Do not execute it as a migration. Flyway is not included.

## 5. Start infrastructure

Ensure these are running:

```text
MySQL       localhost:3306
MongoDB     localhost:27017
RabbitMQ    localhost:5672
```

RabbitMQ check:

```powershell
rabbitmq-diagnostics listeners
```

MongoDB check:

```powershell
mongosh --eval "db.runCommand({ ping: 1 })"
```

## 6. Build backend modules

From the `Kartyavya` project root:

```powershell
mvn clean install
```

This builds `common-contracts` before all services.

## 7. Start services manually in this order

Open a separate terminal for each service.

### 1. Config Server

```powershell
cd config-server
mvn spring-boot:run
```

Verify:

```text
http://localhost:8888/actuator/health
http://localhost:8888/api-gateway/default/main
```

The Config Server JSON must show the remote property files. It must not contain a browser CORS origin of `http://localhost:8083`.

### 2. Eureka Service Registry

```powershell
cd service-registry
mvn spring-boot:run
```

Verify:

```text
http://localhost:8761
```

### 3. Access and Administration Service

```powershell
cd access-admin-service
mvn spring-boot:run
```

### 4. AI Analytics Service

```powershell
cd ai-analytics-service
mvn spring-boot:run
```

### 5. Civic Report Service

```powershell
cd civic-report-service
mvn spring-boot:run
```

### 6. Email Notification Service

```powershell
cd email-notification-service
mvn spring-boot:run
```

### 7. API Gateway

```powershell
cd api-gateway
mvn spring-boot:run
```

All business services should appear as `UP` in Eureka before testing the frontend.

## 8. Install and start the frontend

From `Kartyavya/frontend`:

```powershell
npm install
npm run build
npm run dev
```

Open:

```text
http://localhost:5173
```

The frontend reads the project-root `.env` through `vite.config.js`.

## 9. Verify CORS before login

Run:

```powershell
curl.exe -i -X OPTIONS "http://localhost:8080/api/auth/login" `
  -H "Origin: http://localhost:5173" `
  -H "Access-Control-Request-Method: POST" `
  -H "Access-Control-Request-Headers: content-type"
```

Expected headers:

```text
HTTP/1.1 204 No Content
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
```

The response must never contain:

```text
Access-Control-Allow-Origin: http://localhost:8083
```

## 10. Verify Gemini configuration

This diagnostic endpoint is intentionally public and does not expose the API key:

```text
http://localhost:8080/api/ai/status
```

Expected after entering a valid key and restarting AI Analytics Service:

```json
{
  "mode": "STRICT_AI_NO_KEYWORD_FALLBACK",
  "provider": "Google Gemini Developer API",
  "modelVersion": "gemini:gemini-3.1-flash-lite",
  "configured": true
}
```

`configured: true` confirms the key was loaded. It does not guarantee that the Google request has quota or permission.

Transient `429` and `5xx` responses are retried automatically. If Gemini still fails, the Admin Dashboard continues showing core analytics and map points; only the Gemini hotspot panel enters a degraded state.

## 11. Admin login

The initial admin is created only when the account does not already exist:

```dotenv
KARTYAVYA_ADMIN_EMAIL=admin@kartyavya.local
KARTYAVYA_ADMIN_PASSWORD=Admin@123
```

Changing the password in `.env` does not overwrite an admin already stored in `access_db`. Existing databases retain the existing password hash.

After any JWT secret or role-related change:

1. Restart Access Service, Report Service, AI Service, Email Service and Gateway.
2. Clear the browser session.
3. Log in again to obtain a new JWT.

Browser console command:

```javascript
localStorage.clear();
```

## 12. Configuration change restart rules

Restart the affected process after changing `.env`:

```text
Gateway host/port/CORS/JWT       → restart API Gateway
Config repository settings       → restart Config Server
Database settings                → restart Access and Report services
Gemini settings                  → restart AI Analytics Service
Email settings                   → restart Email Notification Service
Frontend host/port/VITE_*        → restart Vite frontend
JWT/internal key                 → restart all protected backend services and Gateway
```

## 13. Common errors

### Duplicate `.env` key

Example:

```text
Duplicate .env key: API_GATEWAY_HOST
```

Each key may appear only once. Use this from the project root:

```powershell
Get-Content .env |
Where-Object { $_ -match '^[A-Za-z_][A-Za-z0-9_]*=' } |
ForEach-Object { ($_ -split '=', 2)[0].Trim() } |
Group-Object |
Where-Object Count -gt 1
```

The command must return nothing.

### CORS header shows a service port

Confirm:

```dotenv
KARTYAVYA_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Push the included `api-gateway.properties` to the remote Config Repository, stop the old Gateway process, run `mvn clean spring-boot:run`, and hard-refresh the browser.

### `Missing bearer token`

Protected endpoints require login. `/api/ai/status`, login, registration, password recovery, department lookup, complaint tracking and health checks are public.

### `403 Forbidden` for Admin analytics

Clear old browser storage and log in again. This package normalizes authorities to `ROLE_ADMIN`, `ROLE_OFFICER` and `ROLE_CITIZEN` while retaining existing title-case role values in the database and JWT response payload.

### Gemini configured but hotspot analysis unavailable

Check the AI Analytics Service terminal. The service logs the Google HTTP status and a shortened provider response. Typical causes are invalid key permissions, wrong model, quota exhaustion, temporary `429`/`5xx` failure or network timeout.

### Config Server cannot fetch GitHub

Check:

```dotenv
KARTYAVYA_CONFIG_REPO_URI=https://github.com/navinchaudhari/kartyavya-config
KARTYAVYA_CONFIG_REPO_DEFAULT_LABEL=main
```

For a private repository, provide `GITHUB_USERNAME` and `GITHUB_TOKEN` only in `.env`.

## Security notes

- Never commit `.env`.
- Never expose the Config Server, Eureka, MySQL, MongoDB or RabbitMQ directly to the internet.
- Replace default admin, MySQL, JWT and internal-key values before deployment.
- Use HTTPS and a secrets manager for production deployment.
- Keep browser access through the API Gateway rather than directly calling service ports.
