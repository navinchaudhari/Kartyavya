# Exclusive Service and Frontend Ownership (Frozen) - Owner: M1

## Backend - one exclusive owner per Spring application
| Member | Service | No one else edits |
|---|---|---|
| M1 (PL) | config-server/, access-admin-service/, private kartyavya-config repo | - |
| M2 | civic-report-service/ | M5/M6 consume its API, never add backend packages |
| M3 | ai-analytics-service/ | - |
| M4 | email-notification-service/ + RabbitMQ queue/DLQ catalog | - |
| M5 | service-registry/ | - |
| M6 | api-gateway/ | - |

## Frontend - folder-based ownership
| Folder | Owner | Review required |
|---|---|---|
| frontend/src/auth | M5 | M1 (auth contract) |
| frontend/src/public | M5 | - |
| frontend/src/citizen | M5 | - |
| frontend/src/maps | M5 | - |
| frontend/src/officer | M6 | - |
| frontend/src/admin/management | M6 | - |
| frontend/src/admin/analytics | M6 | M3 (contracts) |
| frontend/src/admin/notifications | M4 | - |
| frontend/src/shared/layout | M6 | - |
| frontend/src/shared/security | M6 | M1 (JWT behavior) |

## Non-negotiable ownership rules
- No member creates controllers, entities, repositories or configuration inside another member's service.
- Cross-service collaboration happens only through frozen APIs, Gateway routes, Feign contracts, RabbitMQ
  events and pull-request review - never by jointly editing source folders.
- Any contract change (path, DTO field, enum, event, config key) goes through `docs/contracts/` via a
  `contract/<short-description>` branch, reviewed by PL + every impacted owner, merged before implementation
  branches use the new shape. Breaking RabbitMQ event changes increment `schemaVersion`.

## Standard error contract (every service, every HTTP error)
```json
{
  "timestamp": "2026-07-25T10:30:00Z",
  "status": 400,
  "error": "VALIDATION_FAILED",
  "message": "Request contains invalid fields",
  "path": "/api/reports",
  "correlationId": "uuid",
  "fieldErrors": { "title": "Title is required" }
}
```
Frozen error codes: VALIDATION_FAILED(400), INVALID_REQUEST(400), INVALID_CREDENTIALS(401),
AUTHENTICATION_REQUIRED(401), INTERNAL_SERVICE_AUTH_FAILED(401), ACCESS_DENIED(403), RESOURCE_NOT_FOUND(404),
ROUTING_RULE_NOT_FOUND(404), DUPLICATE_RESOURCE(409), INVALID_STATUS_TRANSITION(409), CONCURRENT_UPDATE(409),
RATE_LIMIT_EXCEEDED(429), SERVICE_UNAVAILABLE(503), INTERNAL_SERVER_ERROR(500).
