# OpenFeign Inter-Service Contracts (Frozen) - Owner: M2/M4 consumer, M1 provider

Only these two synchronous calls are approved in v1. Any new Feign client requires a contract-change review.

## 1. Routing lookup - civic-report-service -> access-admin-service
GET /internal/routing/resolve?category=<ReportCategory>
Headers: X-Internal-Service-Key, X-Correlation-Id
Timeout: connect 1000ms / read 2000ms. Retry: max 2 (connect/5xx only). Circuit breaker id: routingClient.
Fallback: keep report CLASSIFIED, schedule retry.
Response 200 `DepartmentRoutingResponse`: { category, departmentId, departmentName, departmentEmail }

## 2. User contact lookup - email-notification-service -> access-admin-service
GET /internal/users/{userId}/contact
Headers: X-Internal-Service-Key, X-Correlation-Id
Timeout: connect 1000ms / read 2000ms. Retry: max 3 in async consumer, then DLQ.
Response 200 `UserContactResponse`: { userId, name, email, enabled }

## Error mapping (provider status -> consumer exception -> behavior)
400 -> InvalidInternalRequestException -> do not retry, log contract/input defect
401/403 -> InternalServiceAuthenticationException -> do not retry repeatedly, fail integration, alert PL
404 -> RoutingRuleNotFoundException / UserContactNotFoundException -> business-specific fallback, no data fabrication
409 -> RemoteConflictException -> return/record conflict with same correlationId
500/502/503/504 -> RemoteServiceUnavailableException -> retry then circuit-breaker/DLQ

## DTO rule
Feign request/response records live in the consumer service under `integration/client`. Provider JPA entities are
never imported or copied. Field names and types must match this document exactly. WireMock contract tests must
verify success, 404, 503, timeout and malformed-body behavior.
