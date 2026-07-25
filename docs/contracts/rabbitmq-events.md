# RabbitMQ Event Contracts (Frozen) - Owner: M4 catalog, publishers approve

## Topology
Exchange: kartyavya.events (topic). Content type: application/json. Publisher confirms: required.
Consumer ack: manual, after successful idempotent processing. Default retry: 3 attempts with backoff.
Dead-letter rule: every queue has a matching `.dlq` queue. Schema version: 1.0.

## Common envelope
```json
{
  "schemaVersion": "1.0",
  "eventId": "uuid",
  "eventType": "report.created",
  "correlationId": "uuid",
  "reportId": 101,
  "producer": "civic-report-service",
  "occurredAt": "2026-07-25T10:30:00Z",
  "payload": {}
}
```

## Routing keys and queues
| Routing Key | Publisher | Consumer | Queue | DLQ |
|---|---|---|---|---|
| report.created | Civic Report | AI & Analytics | ai.report.created.q | ai.report.created.dlq |
| report.created | Civic Report | Email Notification | email.report.created.q | email.report.created.dlq |
| report.classified | AI & Analytics | Civic Report | report.report.classified.q | report.report.classified.dlq |
| report.classification.corrected | AI & Analytics | Civic Report | report.classification.corrected.q | report.classification.corrected.dlq |
| report.assigned | Civic Report | AI & Analytics | ai.report.assigned.q | ai.report.assigned.dlq |
| report.assigned | Civic Report | Email Notification | email.report.assigned.q | email.report.assigned.dlq |
| report.status.changed | Civic Report | AI & Analytics | ai.report.status.changed.q | ai.report.status.changed.dlq |
| report.status.changed | Civic Report | Email Notification | email.report.status.changed.q | email.report.status.changed.dlq |
| report.resolved | Civic Report | AI & Analytics | ai.report.resolved.q | ai.report.resolved.dlq |
| report.resolved | Civic Report | Email Notification | email.report.resolved.q | email.report.resolved.dlq |

## Payloads (inside `payload`)
report.created: trackingCode, reporterId, title, description, imageUrl, latitude, longitude, createdAt
report.classified: category, severity, confidenceScore, urgencyScore, source, modelVersion, classifiedAt
report.classification.corrected: originalCategory, correctedCategory, correctedByUserId, reason, correctedAt
report.assigned: departmentId, departmentName, departmentEmail, assignedAt
report.status.changed: oldStatus, newStatus, changedByUserId, changedByRole, remarks, changedAt
report.resolved: resolvedByUserId, resolutionRemark, completionImageUrl, resolvedAt

## Idempotency and transaction rule
- Civic Report persists lifecycle events in `outbox_events` in the same MySQL transaction as the report change.
- A scheduled publisher sends PENDING outbox events, marks PUBLISHED only after publisher confirmation.
- Every consumer checks `processed_events`/eventId before changing state or sending email.
- Duplicate events are acknowledged and skipped - never create duplicate classifications, analytics rows or emails.
- Poison messages are retried per policy then dead-lettered with original body + correlationId.
