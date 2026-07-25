# Database and Persistence Contracts (Frozen) - Owner: M1/M2 (MySQL), M3/M4 (MongoDB)

## Ownership matrix
| Service | Database | Tech | Owner | Rule |
|---|---|---|---|---|
| access-admin-service | access_db | MySQL 8.0 | M1 | Only writer of user/department/routing data |
| civic-report-service | report_db | MySQL 8.0 | M2 | Stores external user/department IDs, no cross-DB FK |
| ai-analytics-service | ai_analytics_db | MongoDB | M3 | Consumes events, no direct SQL reads |
| email-notification-service | notification_db | MongoDB | M4 | Consumes events + contact Feign response |

## Migration and ORM rules
- Flyway mandatory for access_db and report_db. Migration files owned only by the exclusive service owner.
- `spring.jpa.hibernate.ddl-auto=validate` everywhere. Never `update` or `create-drop` in shared/docker/prod.
- Every new column/constraint = new immutable migration. Applied migrations are never edited.
- Each MySQL service has an empty-database migration test via Testcontainers.

## access_db (3NF) - tables
users(id, name, email UNIQUE, password_hash, enabled, created_at, updated_at)
roles(id, name UNIQUE)
user_roles(user_id PK+FK, role_id FK) - one role per user in v1
departments(id, name UNIQUE, contact_email, enabled, created_at, updated_at)
officer_department_assignments(id, officer_id FK UNIQUE, department_id FK, assigned_at, active)
routing_rules(id, category UNIQUE, department_id FK, active, created_at, updated_at)

## report_db (3NF) - tables
reports(id, tracking_code UNIQUE, reporter_id [external, no FK], title, description, latitude, longitude,
  category, severity, confidence_score, urgency_score, status, assigned_department_id [external, no FK],
  version [@Version], created_at, updated_at)
report_images(id, report_id FK, image_url, image_type, created_at)
status_history(id, report_id FK, old_status, new_status, changed_by_user_id [external], changed_by_role, remarks, created_at)
resolution_evidence(id, report_id FK UNIQUE, resolution_remark, completion_image_url, resolved_by_user_id [external], resolved_at)
duplicate_report_links(id, source_report_id FK, duplicate_of_report_id FK, distance_meters, created_at)
outbox_events(id UUID PK, aggregate_id, event_type, payload_json, correlation_id, status, attempts, created_at, published_at)

Cross-service references (reporter_id, assigned_department_id, changed_by_user_id) are logical only - never a
foreign key into access_db. Full DDL: see each service's `src/main/resources/db/migration/V1__*.sql`.

## ai_analytics_db (Mongo) collections
classification_results (UNIQUE reportId), classification_corrections (INDEX reportId+createdAt),
report_event_snapshots (UNIQUE eventId, INDEX reportId, optional geo index), processed_events (UNIQUE eventId),
analytics_snapshots (INDEX snapshotType+period)

## notification_db (Mongo) collections
notification_logs (UNIQUE eventId+recipientEmail+notificationType), processed_events (UNIQUE eventId),
failed_notifications (INDEX eventId), email_templates (UNIQUE templateKey+version)
