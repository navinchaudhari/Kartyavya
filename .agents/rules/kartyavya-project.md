# Kartyavya - Antigravity Workspace Rules

You are an AI coding agent working inside the Kartyavya monorepo. These rules are mandatory for every session,
every member, every ticket. If a request conflicts with these rules, stop and ask the human instead of proceeding.

## 0. Read before anything else
Before writing or editing any file, read:
1. docs/contracts/architecture.md
2. docs/contracts/service-catalog.md
3. docs/contracts/member-ownership.md
4. Any other file in docs/contracts/ relevant to the ticket (gateway-routes.md, api-contracts.md,
   feign-contracts.md, rabbitmq-events.md, database-schema.md, config-contracts.md)

## 1. Exclusive ownership - hard boundary
Each member may only create or edit files inside their own exclusive folder(s):
- M1: config-server/, access-admin-service/, docs/contracts/ (owner, team reviews), kartyavya-config (separate repo)
- M2: civic-report-service/
- M3: ai-analytics-service/
- M4: email-notification-service/
- M5: service-registry/, frontend/src/auth/, frontend/src/public/, frontend/src/citizen/, frontend/src/maps/
- M6: api-gateway/, frontend/src/officer/, frontend/src/admin/management/, frontend/src/admin/analytics/,
      frontend/src/shared/layout/, frontend/src/shared/security/
- M4 also owns: frontend/src/admin/notifications/

Never create, modify or delete a file outside the current member's exclusive folder(s), even if it would be
"faster" or "just a small fix." If a change is needed elsewhere, stop and tell the human to open a
contract-change PR or ask the relevant owner.

## 2. Frozen contracts are law
Do not invent or silently change: service IDs, ports, Gateway paths, JSON field names, enum values, DTO shapes,
RabbitMQ routing keys/queues/payload fields, Feign paths/DTOs, database table/column names, or error codes.
If a ticket seems to require a contract change, stop and flag it - do not implement around it.

## 3. Required workflow for every ticket
1. Inspect: current git branch/status, existing implementation, existing tests, relevant docs/contracts files.
2. Plan: produce a file-by-file implementation plan (files to add/edit, APIs, config keys, DB/Mongo changes,
   events, validation, tests, verification commands). Do NOT edit files yet.
3. Wait for explicit human approval of the plan.
4. Implement: only the approved ticket, only inside the owned folder(s), preserving all frozen contracts.
5. Test: run the relevant build/test commands (see below) without suppressing failures.
6. Diff review: check the git diff for unowned files, contract drift, and secrets before proposing a commit.
7. Report: files changed, APIs/config/events affected, test results, and any discovery/Gateway/Feign/RabbitMQ
   evidence relevant to the change.

## 4. Standard commands
```
# Spring services
mvn clean test
mvn clean package
# React frontend
npm ci
npm run lint
npm test -- --run
npm run build
# Integrated environment
docker compose up --build
```

## 5. Non-negotiable technical rules (mirrors docs/contracts/architecture.md Section on non-negotiable rules)
- Java 21, Spring Boot 3.5.16, React 19, Spring Cloud 2025.0.3 BOM are frozen versions.
- No service reads or joins another service's database. No shared JPA entities - only explicit DTOs.
- MySQL schema changes only via new, immutable Flyway migrations. `ddl-auto=validate` always.
- OpenFeign is allowed only for the two approved lookups in feign-contracts.md.
- AI, analytics and email processing always go through RabbitMQ, asynchronously - never block report submission.
- Every HTTP error uses the canonical error JSON in member-ownership.md.
- Secrets are environment variables / secret-store values only - never hard-coded or committed.

## 6. Never do this
- Never add a new Feign client, REST call, or cross-service DB query without a contract-change PR.
- Never hardcode `localhost:<port>` URLs in place of Eureka service IDs.
- Never bypass the API Gateway from the frontend.
- Never edit an already-applied Flyway migration file - add a new one.
- Never touch another member's service folder or frontend module, even for a "quick fix."
