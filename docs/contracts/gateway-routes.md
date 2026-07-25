# API Gateway Route Table (Frozen) - Owner: M6, PL approval required for changes

| Route ID | Path | URI | Access Rule |
|---|---|---|---|
| access-auth | /api/auth/** | lb://access-admin-service | Public register/login; other auth endpoints protected |
| access-admin | /api/admin/users/**, /api/admin/officers/** | lb://access-admin-service | ADMIN |
| departments | /api/departments/**, /api/routing-rules/** | lb://access-admin-service | Authenticated read; ADMIN write |
| reports | /api/reports/** | lb://civic-report-service | Role-dependent |
| public-reports | /api/public/reports/** | lb://civic-report-service | Public tracking only |
| ai | /api/ai/** | lb://ai-analytics-service | Officer/Admin |
| analytics | /api/analytics/** | lb://ai-analytics-service | ADMIN |
| notifications | /api/notifications/** | lb://email-notification-service | Authenticated; admin retry/log APIs |

## Public and protected paths
| Path | Method | Access |
|---|---|---|
| /api/auth/register | POST | Public |
| /api/auth/login | POST | Public |
| /api/public/reports/{trackingCode} | GET | Public |
| /actuator/health | GET | Internal/operations |
| /api/** | Other | JWT required unless explicitly listed |

## JWT contract
Claims: sub (user id, string), email, role (frozen Role enum), departmentId (required for DEPARTMENT_OFFICER,
null otherwise), iss=kartyavya-access-service, aud=kartyavya-api, jti, iat/exp (60-minute default lifetime).

## Trusted headers (Gateway strips client-supplied X-User-* and injects its own)
X-User-Id, X-User-Email, X-User-Role, X-User-Department-Id (officer only), X-Correlation-Id (every request),
X-Internal-Service-Key (Feign caller -> /internal/** only).

## Rate limits
Login: 10/min per IP. Registration: 5/hour per IP. Report submission: 10/hour per user.
General authenticated API: 120/min per user. Public tracking: 60/min per IP.
