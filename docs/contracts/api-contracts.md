# External REST API Contracts (Frozen) - Owner: each service owner; M1 merges

All endpoints are addressed through the API Gateway (port 8080). Global conventions:
- JSON UTF-8, camelCase fields, Content-Type application/json. Use null for missing optional values, never empty strings.
- Pagination: zero-based page, default size 20, max 100. Success responses return the resource/page directly (no wrapper).
- Timestamps: ISO-8601 UTC, e.g. 2026-07-25T10:30:00Z. Coordinates: DECIMAL(9,6) lat/lng.
- API version: `/api` paths, no `/v1`; breaking changes need a new documented version.
- Correlation ID: UUID in `X-Correlation-Id`, created by Gateway when absent.

## Frozen enums
Role: CITIZEN, DEPARTMENT_OFFICER, ADMIN
ReportCategory: POTHOLE, GARBAGE, STREETLIGHT, WATER_LEAKAGE, OTHER
Severity: LOW, MEDIUM, HIGH
ReportStatus: SUBMITTED, CLASSIFIED, ASSIGNED, IN_PROGRESS, RESOLVED, REJECTED
ImageType: SUBMISSION, COMPLETION
NotificationType: REPORT_SUBMITTED, REPORT_CLASSIFIED, REPORT_ASSIGNED, STATUS_CHANGED, REPORT_RESOLVED
DeliveryStatus: PENDING, SENT, FAILED, DEAD_LETTERED
OutboxStatus: PENDING, PUBLISHED, FAILED

## Status transition matrix
SUBMITTED -> CLASSIFIED (AI classification event)
CLASSIFIED -> ASSIGNED (routing lookup succeeds)
ASSIGNED -> IN_PROGRESS (officer in assigned dept or Admin)
IN_PROGRESS -> RESOLVED (officer/Admin, resolution remark + completion image required)
Any non-RESOLVED -> REJECTED (officer for own dept or Admin, reason required)
Any -> Any (ADMIN override only; overrideReason required, full audit entry created)

## 1. Access & Administration (M1) - base lb://access-admin-service
| Method | Path | Role | Response | Status |
|---|---|---|---|---|
| POST | /api/auth/register | Public | UserResponse | 201 |
| POST | /api/auth/login | Public | LoginResponse | 200 |
| GET | /api/auth/me | Authenticated | UserProfileResponse | 200 |
| GET | /api/admin/users | ADMIN | Page<UserResponse> | 200 |
| PATCH | /api/admin/users/{id}/status | ADMIN | UserResponse | 200 |
| POST | /api/admin/officers | ADMIN | UserResponse | 201 |
| GET | /api/admin/officers | ADMIN | Page<OfficerResponse> | 200 |
| GET | /api/departments | Authenticated | List<DepartmentResponse> | 200 |
| POST | /api/departments | ADMIN | DepartmentResponse | 201 |
| PATCH | /api/departments/{id} | ADMIN | DepartmentResponse | 200 |
| GET | /api/routing-rules | ADMIN | List<RoutingRuleResponse> | 200 |
| POST | /api/routing-rules | ADMIN | RoutingRuleResponse | 201 |
| PATCH | /api/routing-rules/{id} | ADMIN | RoutingRuleResponse | 200 |

RegisterRequest: name (2..120), email, password (8..72, upper+lower+digit+special).
LoginResponse: { token, expiresAt, user: { id, name, email, role, departmentId } }

## 2. Civic Report (M2) - base lb://civic-report-service
| Method | Path | Role | Response | Status |
|---|---|---|---|---|
| POST | /api/reports | CITIZEN | ReportDetailsResponse | 201 |
| GET | /api/reports/mine | CITIZEN | Page<ReportSummaryResponse> | 200 |
| GET | /api/reports/{id} | Owner/Officer own dept/ADMIN | ReportDetailsResponse | 200 |
| GET | /api/public/reports/{trackingCode} | Public | PublicReportResponse | 200 |
| GET | /api/reports/nearby | Authenticated | List<NearbyReportResponse> | 200 |
| POST | /api/reports/check-duplicate | CITIZEN | DuplicateCheckResponse | 200 |
| GET | /api/reports/department | DEPARTMENT_OFFICER | Page<ReportSummaryResponse> | 200 |
| GET | /api/reports | ADMIN | Page<ReportSummaryResponse> | 200 |
| PATCH | /api/reports/{id}/status | Officer own dept/ADMIN | ReportDetailsResponse | 200 |

CreateReportRequest: title (5..150), description (20..2000), imageUrl (https, <=512 chars), latitude (-90..90), longitude (-180..180).
StatusUpdateRequest: newStatus, remarks, resolutionRemark* , completionImageUrl* (*required when RESOLVED), overrideReason (required for ADMIN override).
nearby: radiusKm default 2, range 0.1..20. Duplicate threshold: 200m, same category when known, unresolved only.

## 3. AI & Analytics (M3) - base lb://ai-analytics-service
| Method | Path | Role | Response | Status |
|---|---|---|---|---|
| GET | /api/ai/reports/{reportId} | Officer/Admin | ClassificationResponse | 200 |
| POST | /api/ai/corrections | Officer/Admin | ClassificationCorrectionResponse | 201 |
| GET | /api/analytics/summary | ADMIN | AnalyticsSummaryResponse | 200 |
| GET | /api/analytics/by-category | ADMIN | List<CategoryMetricResponse> | 200 |
| GET | /api/analytics/by-department | ADMIN | List<DepartmentMetricResponse> | 200 |
| GET | /api/analytics/trend | ADMIN | List<MonthlyTrendResponse> | 200 |
| GET | /api/analytics/heatmap | ADMIN | List<HeatPointResponse> | 200 |
| GET | /api/analytics/problematic-areas | ADMIN | List<AreaVolumeResponse> | 200 |
| GET | /api/analytics/ai-accuracy | ADMIN | AiAccuracyResponse | 200 |

## 4. Email Notification (M4) - base lb://email-notification-service
| Method | Path | Role | Response | Status |
|---|---|---|---|---|
| GET | /api/notifications/mine | Authenticated | Page<NotificationResponse> | 200 |
| GET | /api/notifications | ADMIN | Page<NotificationResponse> | 200 |
| POST | /api/notifications/{id}/retry | ADMIN | NotificationResponse | 202 |

No public "send email" API exists - email is triggered only by approved RabbitMQ events.
