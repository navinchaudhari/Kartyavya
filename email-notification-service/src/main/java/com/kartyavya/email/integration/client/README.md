# 📧 Kartyavya - Email Notification Service

## Overview

The **Email Notification Service** is a dedicated microservice in the **Kartyavya Civic Grievance Management System** responsible for processing notification events and delivering email notifications to users.

This service follows an **event-driven architecture** using **RabbitMQ**. Instead of exposing APIs to create notifications, it listens for events published by other microservices, processes those events, sends email notifications, and stores notification history in MongoDB.

The service is completely independent and communicates asynchronously with other services, improving scalability, reliability, and fault tolerance.

---

# Purpose

The Email Notification Service is responsible for:

- Sending email notifications to users.
- Consuming events from RabbitMQ.
- Processing notification templates.
- Maintaining notification delivery logs.
- Preventing duplicate event processing.
- Retrying failed email deliveries automatically.
- Storing notification history in MongoDB.

---

# Responsibilities

This microservice performs the following responsibilities:

- Consume Report Created events.
- Consume Report Assigned events.
- Consume Report Status Changed events.
- Consume Report Resolved events.
- Render email templates dynamically.
- Send emails using Gmail SMTP.
- Store successful notification logs.
- Store failed notifications for retry.
- Retry failed notifications using Scheduler.
- Prevent duplicate event processing using Idempotency.
- Expose REST APIs for viewing notification history.

---

# Features

- Event-driven architecture
- RabbitMQ Consumers
- MongoDB Persistence
- Gmail SMTP Integration
- Dynamic Email Templates
- Retry Mechanism
- Dead Letter Queue (DLQ) Support
- Notification History
- Idempotent Event Processing
- Swagger/OpenAPI Documentation
- Global Exception Handling
- Eureka Client Registration
- Spring Cloud Config Support

---

# Architecture

The Email Notification Service acts only as an **event consumer**.

```
                 +----------------------+
                 |   Report Service     |
                 +----------+-----------+
                            |
                     Publish Event
                            |
                            ▼
                    RabbitMQ Exchange
                            |
        -----------------------------------------
        |             |             |            |
        ▼             ▼             ▼            ▼

 Report Created   Report Assigned  Status Changed  Report Resolved
      Queue             Queue            Queue           Queue

        |               |               |               |
        +---------------+---------------+---------------+
                                |
                                ▼

                 Email Notification Service

                                |
             ------------------------------------
             |                                  |
             ▼                                  ▼

      Gmail SMTP Server                 MongoDB Database

             |                                  |
             ▼                                  ▼

      Email Delivered                Notification History
```

---

# Notification Workflow

```
Report Service

        │

        ▼

Publish RabbitMQ Event

        │

        ▼

RabbitMQ Exchange

        │

        ▼

Notification Consumer

        │

        ▼

NotificationService

        │

        ▼

Load Email Template

        │

        ▼

Generate Email Content

        │

        ▼

Send Email

        │

        ▼

Save Notification Log

        │

        ▼

Mark Event Processed
```

---

# Event Types

The service currently supports the following notification events.

| Event | Purpose |
|--------|----------|
| ReportCreatedEvent | Sent when a citizen creates a complaint. |
| ReportAssignedEvent | Sent when a complaint is assigned to an officer. |
| ReportStatusChangedEvent | Sent whenever complaint status changes. |
| ReportResolvedEvent | Sent after complaint resolution. |

---

# Technology Stack

| Technology | Version |
|------------|----------|
| Java | 21 |
| Spring Boot | 3.x |
| Spring Cloud | 2025.x |
| Maven | 3.x |
| MongoDB | 8.x |
| RabbitMQ | 4.x |
| Gmail SMTP | SMTP TLS |
| OpenAPI | Swagger 2.8.9 |
| Lombok | Latest |
| Eureka Client | Spring Cloud Netflix |
| Spring Validation | Jakarta Validation |

---

# Microservice Information

| Property | Value |
|----------|-------|
| Service Name | email-notification-service |
| Service Type | Consumer Microservice |
| Port | 8084 |
| Database | MongoDB |
| Communication | RabbitMQ |
| Notification Channel | Email |
| Discovery | Eureka Server |
| Configuration | Spring Cloud Config |

---

# Communication Pattern

This service **does not create notifications through REST APIs**.

Instead, notifications are generated automatically whenever another microservice publishes an event to RabbitMQ.

```
Producer Service
        │
        ▼
RabbitMQ
        │
        ▼
Email Notification Service
        │
        ▼
Email Sent
        │
        ▼
MongoDB
```

---

# Project Goals

The objective of this service is to provide a centralized notification system that can:

- Process events asynchronously.
- Improve scalability.
- Decouple producer and consumer services.
- Maintain notification history.
- Ensure reliable email delivery.
- Retry failed notifications automatically.
- Prevent duplicate notifications.

---

# Future Enhancements

Possible future improvements include:

- SMS Notifications
- Push Notifications
- WhatsApp Notifications
- Kafka Integration
- Multi-language Email Templates
- Notification Preferences
- Email Analytics Dashboard
- Bulk Email Processing
- Template Management UI
- Attachment Support

---
# Project Structure

```
src
├── main
│   ├── java
│   │   └── com.kartyavya.email
│   │       ├── config
│   │       ├── constant
│   │       ├── controller
│   │       ├── dto
│   │       │    ├── event
│   │       │    └── response
│   │       ├── entity
│   │       ├── enums
│   │       ├── exception
│   │       ├── integration.client
│   │       ├── mail
│   │       ├── mapper
│   │       ├── rabbitmq
│   │       ├── repository
│   │       ├── scheduler
│   │       ├── security
│   │       ├── service
│   │       ├── service.impl
│   │       ├── template
│   │       └── util
│   └── resources
│       ├── application.yml
│       └── bootstrap.yml
└── pom.xml
```

---

# Package Description

## config

Contains all Spring configuration classes used by the application.

### MongoAuditConfig

Purpose

- Enables MongoDB auditing.
- Automatically maintains audit fields.

Responsibilities

- Enable `@CreatedDate`
- Enable `@LastModifiedDate`
- Register auditing support.

---

### RabbitMQConfig

Purpose

Creates RabbitMQ infrastructure during application startup.

Responsibilities

- Create Exchanges
- Create Queues
- Create Dead Letter Queues
- Configure Bindings
- Configure Routing Keys
- Configure Listener Container

---

### SwaggerConfig

Purpose

Configures OpenAPI (Swagger UI).

Responsibilities

- API documentation
- Service information
- API version
- Contact details

---

# constant

Contains reusable application constants.

Using constants avoids hardcoded values throughout the project.

---

## AppConstants

Stores application-wide constants.

Examples

- Date formats
- Default values
- Common strings

---

## CollectionConstants

Stores MongoDB collection names.

Examples

- notification_logs
- failed_notifications
- email_templates
- processed_events

Changing a collection name requires updating only this class.

---

## ErrorMessages

Centralizes all exception messages.

Examples

- Notification not found
- Duplicate event
- Template not found
- Email sending failed

---

## MailConstants

Contains email-related constants.

Examples

- Subject placeholders
- HTML placeholders
- Default sender
- Variable names

---

## RabbitMQConstants

Stores RabbitMQ constants.

Examples

- Exchange names
- Queue names
- Dead Letter Exchange
- Routing Keys

---

# controller

Contains REST controllers.

---

## NotificationController

Purpose

Provides APIs for reading notification history.

Responsibilities

- Get all notifications
- Get notification by ID
- Retry failed notification
- Get logged notifications

It does NOT create notifications.

Notifications are generated only through RabbitMQ events.

---

# dto

DTO stands for Data Transfer Object.

DTOs transfer data between layers.

---

## dto.event

Contains RabbitMQ event payloads.

---

### BaseEvent

Base class for all RabbitMQ events.

Common fields

- eventId
- correlationId
- timestamp

---

### ReportCreatedEvent

Represents report creation.

Consumed when a citizen submits a complaint.

---

### ReportAssignedEvent

Represents report assignment.

Consumed when an officer is assigned.

---

### ReportStatusChangedEvent

Represents status updates.

Examples

- OPEN
- IN_PROGRESS
- CLOSED

---

### ReportResolvedEvent

Represents report resolution.

Triggers final notification email.

---

## dto.response

Contains REST response DTOs.

---

### NotificationResponse

Returned by GET APIs.

Contains

- Notification ID
- Recipient
- Subject
- Body
- Status
- Notification Type
- Created Date

---

# entity

Represents MongoDB documents.

---

## BaseEntity

Parent class for all entities.

Contains common audit fields.

Typical fields

- createdAt
- updatedAt

---

## EmailTemplate

Stores email templates.

Purpose

Generate dynamic email body and subject.

Example

Subject

```
Report Assigned
```

Body

```
Hello ${name},

Your complaint has been assigned.

Regards,
Kartyavya
```

---

## FailedNotification

Stores failed email attempts.

Used by Retry Scheduler.

Contains

- Email
- Subject
- Body
- Retry Count
- Failure Reason

---

## NotificationLog

Stores successfully processed notifications.

Purpose

Maintain notification history.

Typical fields

- Recipient
- Subject
- Body
- Status
- Sent Time
- Event ID
- Correlation ID

---

## ProcessedEvent

Stores processed RabbitMQ events.

Purpose

Prevent duplicate processing.

Whenever an event is consumed successfully, its Event ID is stored here.

---

# enums

Stores application enums.

---

## NotificationChannel

Specifies notification medium.

Examples

- EMAIL

Future

- SMS
- PUSH
- WHATSAPP

---

## NotificationStatus

Represents delivery status.

Examples

- SUCCESS
- FAILED
- PENDING

---

## NotificationType

Represents business notification type.

Examples

- REPORT_CREATED
- REPORT_ASSIGNED
- REPORT_STATUS_CHANGED
- REPORT_RESOLVED

---

## RetryStatus

Represents retry state.

Examples

- PENDING
- RETRYING
- COMPLETED
- FAILED

---

## TemplateType

Represents email template category.

Maps templates to notification events.

---

# exception

Contains custom exceptions.

---

## DuplicateEventException

Thrown when an event is processed twice.

---

## EmailSendingException

Thrown when SMTP fails.

---

## NotificationException

Base notification exception.

---

## NotificationNotFoundException

Thrown when notification ID does not exist.

---

## TemplateNotFoundException

Thrown when required email template is unavailable.

---

## GlobalExceptionHandler

Handles all application exceptions.

Converts exceptions into consistent REST responses.

---

# integration.client

Reserved package for future REST clients.

Currently empty.

Can later contain

- Report Service Client
- User Service Client

if synchronous communication becomes necessary.

---

# mail

Contains email sending implementation.

---

## EmailService

Interface for sending emails.

---

## EmailServiceImpl

Implementation using Spring JavaMailSender.

Responsibilities

- Build MIME email
- Send HTML email
- Handle SMTP exceptions

---

# mapper

Contains object mapping utilities.

---

## NotificationMapper

Converts

Entity ⇄ DTO

Used by controller responses.

---

# repository

MongoDB Repository Layer.

All repositories extend MongoRepository.

---

## EmailTemplateRepository

Performs CRUD on email templates.

---

## FailedNotificationRepository

Stores failed notifications.

---

## NotificationLogRepository

Stores notification history.

---

## ProcessedEventRepository

Stores processed event IDs.

---

# scheduler

Contains scheduled background jobs.

---

## FailedNotificationRetryScheduler

Runs automatically.

Responsibilities

- Fetch failed notifications
- Retry sending email
- Update retry count
- Delete on success

---

# security

Reserved package.

Currently empty.

Can be extended later for

- JWT
- OAuth2
- Authentication

---

# service

Business layer interfaces.

---

## NotificationService

Core notification processing.

---

## NotificationLogService

Read notification history.

---

## RetryService

Manage retry operations.

---

## TemplateService

Load email templates.

---

## IdempotencyService

Prevent duplicate event processing.

---

# service.impl

Implementation of all service interfaces.

---

## NotificationServiceImpl

Core business logic.

Coordinates

Consumer

↓

Template

↓

Email

↓

MongoDB

↓

Processed Event

---

## NotificationLogServiceImpl

Fetches notification history.

---

## RetryServiceImpl

Retries failed notifications.

---

## TemplateServiceImpl

Loads templates from MongoDB.

---

## IdempotencyServiceImpl

Checks

```
Already Processed?
        │
       Yes
        │
 Ignore Event
```

---

# template

Reserved package.

Can later contain

- HTML templates
- Thymeleaf templates

---

# util

Contains helper utilities.

---

## DateTimeUtil

Date conversion utilities.

---

## EmailTemplateUtil

Replaces placeholders.

Example

```
${citizenName}
```

↓

```
John Doe
```

---

## JsonUtil

JSON serialization and deserialization.

---

## StringUtil

Common string helper methods.

Examples

- Null checks
- Empty checks
- Formatting

---
# RabbitMQ Configuration

The Email Notification Service follows an **Event-Driven Architecture** using RabbitMQ.

It does **not** receive notification requests through REST APIs.

Instead, it consumes events published by other microservices (primarily the Report Service) and performs email notification processing asynchronously.

---

# Why RabbitMQ?

Using RabbitMQ provides the following benefits:

- Loose coupling between services
- Asynchronous communication
- Better scalability
- Reliable message delivery
- Retry support
- Dead Letter Queue (DLQ)
- Independent deployment of microservices

---

# High-Level Architecture

```

```
                Producer Microservice
                (Report Service)

                       │
                       │ Publish Event
                       ▼

                RabbitMQ Exchange
                kartyavya.events

                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼

 report.created   report.assigned   report.status.changed
        │              │              │
        ▼              ▼              ▼

 Created Queue   Assigned Queue   Status Queue
        │              │              │
        └──────────────┼──────────────┘
                       │
                       ▼

          Email Notification Service

                       │
                       ▼

             NotificationServiceImpl

                       │
          ┌────────────┴─────────────┐
          ▼                          ▼

     Gmail SMTP               MongoDB
```

---

# RabbitMQ Components

The service creates the following RabbitMQ components during application startup.

## Exchanges

### Primary Exchange

```
kartyavya.events
```

Type

```
Topic Exchange
```

Purpose

Receives all notification events published by producer services.

---

### Dead Letter Exchange

```
kartyavya.events.dlx
```

Purpose

Stores failed messages that cannot be processed successfully.

---

# Queues

The Email Notification Service listens to four business queues.

| Queue | Purpose |
|---------|----------|
| email.report.created.q | Report Created notifications |
| email.report.assigned.q | Report Assigned notifications |
| email.report.status.changed.q | Report Status Changed notifications |
| email.report.resolved.q | Report Resolved notifications |

---

# Dead Letter Queues (DLQ)

Each business queue has its own Dead Letter Queue.

| DLQ | Purpose |
|------|----------|
| email.report.created.dlq | Failed Report Created events |
| email.report.assigned.dlq | Failed Report Assigned events |
| email.report.status.changed.dlq | Failed Status Changed events |
| email.report.resolved.dlq | Failed Report Resolved events |

If a consumer repeatedly fails to process a message, RabbitMQ routes it to the corresponding DLQ.

---

# Routing Keys

Routing Keys determine which queue receives the message.

| Routing Key | Queue |
|-------------|----------------------------|
| report.created | email.report.created.q |
| report.assigned | email.report.assigned.q |
| report.status.changed | email.report.status.changed.q |
| report.resolved | email.report.resolved.q |

---

# Queue Binding

```
Exchange

kartyavya.events

        │

        ├──────────── report.created
        │
        ▼

email.report.created.q

        │

        ├──────────── report.assigned
        │
        ▼

email.report.assigned.q

        │

        ├──────────── report.status.changed
        │
        ▼

email.report.status.changed.q

        │

        ├──────────── report.resolved
        │
        ▼

email.report.resolved.q
```

---

# Configuration Class

RabbitMQ infrastructure is configured inside

```
RabbitMQConfig
```

Responsibilities

- Create Exchange
- Create DLX
- Create Queues
- Create DLQs
- Bind Queues
- Configure Listener Container
- Configure Message Converter

---

# Consumers

Every queue has a dedicated consumer.

## AbstractNotificationConsumer

Parent class for all consumers.

Purpose

Provides common notification processing functionality shared by all consumers.

Responsibilities

- Common validation
- Common exception handling
- Shared notification processing logic

---

## ReportCreatedConsumer

Consumes

```
ReportCreatedEvent
```

Workflow

```
RabbitMQ

↓

Receive Event

↓

NotificationService

↓

Send Email

↓

Save Notification Log
```

---

## ReportAssignedConsumer

Consumes

```
ReportAssignedEvent
```

Workflow

Officer assigned

↓

Receive Event

↓

Generate Email

↓

Send Email

↓

Save Notification

---

## ReportStatusChangedConsumer

Consumes

```
ReportStatusChangedEvent
```

Examples

- OPEN
- IN_PROGRESS
- CLOSED

Whenever report status changes,

an email notification is sent.

---

## ReportResolvedConsumer

Consumes

```
ReportResolvedEvent
```

This is generally the final notification sent to the citizen after complaint resolution.

---

# Event Processing Flow

Every RabbitMQ consumer follows the same workflow.

```
RabbitMQ Message

        │

        ▼

Receive Event

        │

        ▼

Validate Event

        │

        ▼

Duplicate Check

        │

        ▼

Load Email Template

        │

        ▼

Replace Variables

        │

        ▼

Generate Subject

        │

        ▼

Generate Email Body

        │

        ▼

Send Email

        │

        ▼

Save NotificationLog

        │

        ▼

Store ProcessedEvent
```

---

# NotificationService Flow

The NotificationServiceImpl coordinates the entire business logic.

```
Consumer

↓

NotificationService

↓

TemplateService

↓

EmailService

↓

NotificationLogRepository

↓

ProcessedEventRepository
```

NotificationService is responsible for:

- Event validation
- Idempotency check
- Template loading
- Email generation
- Email sending
- Notification logging
- Event completion

---

# Duplicate Event Prevention

Duplicate processing is prevented using

```
ProcessedEvent
```

collection.

Workflow

```
Receive Event

        │

        ▼

Event ID Exists?

       /  \

     Yes   No

     │      │

 Ignore   Continue

            │

            ▼

 Store Event ID
```

This ensures every event is processed only once.

---

# Retry Mechanism

If email sending fails,

the notification is stored inside

```
FailedNotification
```

instead of being lost.

Workflow

```
Email Sending

        │

        ▼

Failure

        │

        ▼

FailedNotification Collection

        │

        ▼

Retry Scheduler

        │

        ▼

Retry Email

        │

   Success?

    /     \

  Yes      No

  │         │

Delete     Increment Retry Count
```

---

# Scheduler

Background scheduler

```
FailedNotificationRetryScheduler
```

Responsibilities

- Read failed notifications
- Retry email delivery
- Update retry count
- Save NotificationLog
- Delete successful retry

Runs automatically based on the configured schedule.

---

# Email Delivery Flow

```
NotificationService

        │

        ▼

TemplateService

        │

        ▼

Generate Subject

        │

        ▼

Generate HTML Body

        │

        ▼

EmailServiceImpl

        │

        ▼

JavaMailSender

        │

        ▼

SMTP Server

        │

        ▼

Recipient
```

---

# Failure Handling

If any error occurs

```
SMTP Down

Invalid Email

Authentication Failure

Template Missing

Duplicate Event
```

appropriate custom exceptions are thrown and handled by

```
GlobalExceptionHandler
```

---

# End-to-End Notification Flow

```
Citizen creates Report

        │

        ▼

Report Service

        │

Publish ReportCreatedEvent

        │

        ▼

RabbitMQ Exchange

        │

        ▼

ReportCreatedConsumer

        │

        ▼

NotificationServiceImpl

        │

        ▼

Load Template

        │

        ▼

Generate Email

        │

        ▼

Send Email

        │

        ▼

NotificationLog

        │

        ▼

ProcessedEvent
```

---

# RabbitMQ Verification

Open RabbitMQ Management Console

```
http://localhost:15672
```

Verify:

- Exchange exists
- DLX exists
- Queues are created
- DLQs are created
- Routing Keys are correct
- Bindings exist
- Queue message count
- Consumers are active

---

# Expected Startup Sequence

Start services in the following order:

1. MongoDB
2. RabbitMQ
3. Config Server
4. Eureka Server
5. Email Notification Service
6. Producer Services (Report Service)

Following this sequence ensures that all required infrastructure is available before consumers begin listening for events.

---
# MongoDB Configuration

The Email Notification Service uses **MongoDB** as its primary database for storing notification-related information.

Unlike the Report Service or User Service, this microservice does **not** use a relational database because notification data is document-oriented, schema-flexible, and optimized for fast read/write operations.

---

# Database

```
notification_db
```

---

# Collections

The service maintains four primary MongoDB collections.

| Collection | Purpose |
|------------|---------|
| notification_logs | Stores successfully processed email notifications. |
| failed_notifications | Stores failed email notifications for retry. |
| email_templates | Stores email templates used for generating dynamic emails. |
| processed_events | Stores processed event IDs to prevent duplicate processing. |

---

# Entity Relationship

```
                RabbitMQ Event
                      │
                      ▼
             NotificationServiceImpl
                      │
     ┌────────────────┼────────────────┐
     │                │                │
     ▼                ▼                ▼

EmailTemplate   NotificationLog   ProcessedEvent
                      │
                      ▼
           FailedNotification
         (Only if email fails)
```

---

# BaseEntity

## Purpose

`BaseEntity` is the parent class for all MongoDB entities.

It contains common audit information shared across multiple collections.

---

## Responsibilities

- Automatically maintain creation timestamp
- Automatically maintain modification timestamp
- Reduce duplicate code

---

## Common Fields

| Field | Description |
|---------|-------------|
| createdAt | Date and time when document is created |
| updatedAt | Last modification timestamp |

---

# NotificationLog

## Purpose

Stores every successfully processed email notification.

This collection acts as the notification history of the application.

---

## Responsibilities

- Store recipient information
- Store email subject
- Store email body
- Store notification status
- Store notification type
- Store event information
- Maintain audit history

---

## Typical Fields

| Field | Description |
|---------|-------------|
| id | MongoDB ObjectId |
| eventId | RabbitMQ Event Identifier |
| recipientEmail | Receiver Email |
| notificationType | Business Event Type |
| status | SUCCESS / FAILED |
| subject | Email Subject |
| body | Email Content |
| correlationId | Trace ID across services |
| sentAt | Email Delivery Timestamp |
| createdAt | Record Creation Time |

---

## Sample Document

```json
{
  "_id": "...",
  "eventId": "EVT-1001",
  "recipientEmail": "citizen@example.com",
  "notificationType": "REPORT_CREATED",
  "status": "SUCCESS",
  "subject": "Complaint Registered",
  "body": "Your complaint has been registered successfully.",
  "correlationId": "CORR-1001",
  "sentAt": "2026-07-27T10:15:00",
  "createdAt": "2026-07-27T10:15:00"
}
```

---

# FailedNotification

## Purpose

Stores email notifications that could not be delivered.

Instead of losing failed notifications, they are persisted and retried later by the scheduler.

---

## Responsibilities

- Store failed email
- Store retry count
- Store failure reason
- Store notification payload

---

## Typical Fields

| Field | Description |
|---------|-------------|
| recipientEmail | Receiver |
| subject | Email Subject |
| body | Email Body |
| notificationType | Event Type |
| correlationId | Request Tracking |
| retryCount | Number of retry attempts |
| retryStatus | Retry Status |

---

## Retry Flow

```
Email Sending

      │

      ▼

Failure

      │

      ▼

FailedNotification Collection

      │

      ▼

Retry Scheduler

      │

      ▼

Email Sent Successfully

      │

      ▼

NotificationLog
```

---

# EmailTemplate

## Purpose

Stores reusable email templates.

The Notification Service never hardcodes email content.

Instead, it loads templates dynamically from MongoDB.

---

## Why Templates?

Without templates,

every email would require Java code changes.

Using templates allows:

- Dynamic email generation
- Easy content updates
- No code modification
- Better maintainability

---

## Template Structure

Each template contains

- Template Name
- Subject
- Email Body
- Template Type

---

## Example

Subject

```
Complaint Assigned
```

Body

```
Hello ${citizenName},

Your complaint has been assigned successfully.

Complaint Number : ${complaintId}

Status : ${status}

Thank You.

Team Kartyavya
```

---

# Variable Replacement

During processing,

placeholders are replaced with actual values.

Example

Before

```
Hello ${citizenName}
```

After

```
Hello Rahul Patil
```

This functionality is handled by

```
EmailTemplateUtil
```

---

# ProcessedEvent

## Purpose

Ensures every RabbitMQ event is processed only once.

---

## Why?

RabbitMQ guarantees delivery,

which means duplicate messages are possible.

Without duplicate detection,

users could receive multiple identical emails.

---

## Workflow

```
Receive Event

      │

      ▼

Event Exists?

   /        \

 Yes         No

 │            │

Ignore     Process Event

             │

             ▼

Save Event ID
```

---

# DTO Layer

DTOs are used to transfer data between application layers without exposing database entities.

---

# Event DTOs

Located under

```
dto.event
```

These classes represent RabbitMQ messages.

---

## BaseEvent

Parent DTO for all notification events.

Contains common information.

Typical Fields

- eventId
- correlationId
- timestamp

---

## ReportCreatedEvent

Published when a new complaint is registered.

---

## ReportAssignedEvent

Published when an officer is assigned.

---

## ReportStatusChangedEvent

Published whenever complaint status changes.

---

## ReportResolvedEvent

Published after complaint resolution.

---

# Response DTO

Located under

```
dto.response
```

---

## NotificationResponse

Returned by REST APIs.

Purpose

Convert NotificationLog entity into API-friendly response.

---

## Returned Fields

| Field | Description |
|---------|-------------|
| id | Notification ID |
| eventId | Event Identifier |
| recipientEmail | Email Address |
| notificationType | Business Event |
| status | Notification Status |
| subject | Email Subject |
| body | Email Body |
| correlationId | Trace Identifier |
| sentAt | Delivery Time |
| createdAt | Creation Time |

---

# Notification Life Cycle

```
RabbitMQ Event

      │

      ▼

NotificationService

      │

      ▼

Load Template

      │

      ▼

Generate Email

      │

      ▼

Send Email

      │

      ▼

NotificationLog

      │

      ▼

REST API Response
```

---

# Business Data Flow

```
Producer Service

        │

        ▼

RabbitMQ Event

        │

        ▼

Event DTO

        │

        ▼

NotificationService

        │

        ▼

EmailTemplate

        │

        ▼

EmailService

        │

        ▼

NotificationLog

        │

        ▼

NotificationResponse

        │

        ▼

REST Client
```

---

# MongoDB Verification

Open MongoDB Shell

```javascript
show dbs

use notification_db

show collections
```

Expected Collections

```
notification_logs
failed_notifications
email_templates
processed_events
```

View Notification Logs

```javascript
db.notification_logs.find().pretty()
```

View Failed Notifications

```javascript
db.failed_notifications.find().pretty()
```

View Email Templates

```javascript
db.email_templates.find().pretty()
```

View Processed Events

```javascript
db.processed_events.find().pretty()
```

---

# Data Validation

Before deployment, verify:

- NotificationLog documents are stored after successful email delivery.
- FailedNotification documents are created when email sending fails.
- EmailTemplate documents exist for all supported notification types.
- ProcessedEvent documents are created after successful processing.
- GET APIs return NotificationResponse objects instead of exposing entity classes directly.

---
# REST API Documentation

The Email Notification Service exposes REST APIs only for viewing and managing notification records.

> **Note:** This service does **not** expose APIs to create notifications. Notification records are created automatically when RabbitMQ events are consumed.

---

# Base URL

```
http://localhost:8084
```

---

# Swagger UI

After the application starts successfully, open:

```
http://localhost:8084/swagger-ui/index.html
```

Swagger provides:

- Interactive API documentation
- Request testing
- Response preview
- Status code information

---

# Available APIs

| Method | Endpoint | Description |
|----------|----------|-------------|
| GET | /api/notifications | Retrieve all notification logs |
| GET | /api/notifications/{id} | Retrieve notification by MongoDB ID |
| GET | /api/notifications/mine | Retrieve notifications for the current user *(implementation depends on authentication)* |
| POST | /api/notifications/{id}/retry | Retry sending a failed notification |

---

# API Details

---

## 1. Get All Notifications

### Endpoint

```
GET /api/notifications
```

### Purpose

Returns every notification stored in MongoDB.

### Request

```
GET http://localhost:8084/api/notifications
```

### Success Response

HTTP Status

```
200 OK
```

Example

```json
[
  {
    "id": "6a67000fabd5ffe1d5abc114",
    "eventId": "EVT-1001",
    "recipientEmail": "citizen@example.com",
    "notificationType": "REPORT_ASSIGNED",
    "status": "SUCCESS",
    "subject": "Report Assigned",
    "body": "Your complaint has been assigned.",
    "correlationId": "CORR-1001",
    "sentAt": "2026-07-27T10:15:00",
    "createdAt": "2026-07-27T10:15:00"
  }
]
```

---

## Verification

Check

- MongoDB connection
- NotificationLogRepository
- NotificationLogService
- NotificationMapper
- NotificationController

---

## 2. Get Notification By ID

### Endpoint

```
GET /api/notifications/{id}
```

Example

```
GET http://localhost:8084/api/notifications/6a67000fabd5ffe1d5abc114
```

---

### Success Response

HTTP

```
200 OK
```

Example

```json
{
  "id": "6a67000fabd5ffe1d5abc114",
  "eventId": "EVT-1001",
  "recipientEmail": "citizen@example.com",
  "notificationType": "REPORT_ASSIGNED",
  "status": "SUCCESS",
  "subject": "Report Assigned",
  "body": "Your complaint has been assigned.",
  "correlationId": "CORR-1001",
  "sentAt": "2026-07-27T10:15:00",
  "createdAt": "2026-07-27T10:15:00"
}
```

---

### Invalid ID

Example

```
GET /api/notifications/1234567890abcdef12345678
```

If record does not exist

```
404 Not Found
```

Example Response

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Notification not found"
}
```

---

## 3. Get My Notifications

### Endpoint

```
GET /api/notifications/mine
```

### Purpose

Returns notifications belonging to the authenticated user.

> **Note:** This endpoint requires integration with the Authentication/User Service to identify the logged-in user. If authentication is not yet integrated, the endpoint may return an empty response or remain inactive.

---

## 4. Retry Failed Notification

### Endpoint

```
POST /api/notifications/{id}/retry
```

### Purpose

Retries sending a previously failed email notification.

### Workflow

```
Client

      │

      ▼

Retry API

      │

      ▼

RetryService

      │

      ▼

EmailService

      │

      ▼

NotificationLog

      │

      ▼

Delete FailedNotification
```

---

# HTTP Status Codes

| Status | Meaning |
|----------|----------|
| 200 | Request processed successfully |
| 400 | Invalid request |
| 404 | Notification not found |
| 409 | Duplicate event detected |
| 500 | Internal server error |

---

# Exception Handling

The application uses a centralized exception handler.

```
GlobalExceptionHandler
```

Supported Exceptions

| Exception | HTTP Status |
|------------|-------------|
| NotificationNotFoundException | 404 |
| TemplateNotFoundException | 404 |
| DuplicateEventException | 409 |
| NotificationException | 400 |
| EmailSendingException | 500 |
| Exception | 500 |

---

# API Testing

## Using Swagger

1. Start the application.
2. Open Swagger UI.
3. Select the endpoint.
4. Click **Try it out**.
5. Execute the request.
6. Verify the response.

---

## Using Postman

### Get All Notifications

```
GET

http://localhost:8084/api/notifications
```

---

### Get Notification By ID

```
GET

http://localhost:8084/api/notifications/{id}
```

Replace

```
{id}
```

with a valid MongoDB ObjectId.

---

# MongoDB Verification

Open MongoDB Shell

```javascript
use notification_db

db.notification_logs.find().pretty()
```

Verify that the returned documents match the REST API response.

---

# End-to-End Verification

The following sequence verifies the complete Email Notification Service.

### Step 1

Start

- MongoDB
- RabbitMQ
- Config Server
- Eureka Server
- Email Notification Service

---

### Step 2

Verify Eureka Registration

Open

```
http://localhost:8761
```

Ensure

```
EMAIL-NOTIFICATION-SERVICE
```

appears with status **UP**.

---

### Step 3

Verify RabbitMQ

Open

```
http://localhost:15672
```

Check

- Exchange
- Queues
- Bindings
- Consumers

---

### Step 4

Verify MongoDB

```
show dbs

use notification_db

show collections
```

---

### Step 5

Insert a sample NotificationLog document manually (optional, for testing GET APIs before integration).

---

### Step 6

Call

```
GET /api/notifications
```

Expected

```
200 OK
```

---

### Step 7

Copy the returned ObjectId.

---

### Step 8

Call

```
GET /api/notifications/{id}
```

Expected

```
200 OK
```

---

### Step 9

Use an invalid ObjectId.

Expected

```
404 Not Found
```

---

# Integration Testing

After the Report Service is integrated:

```
Citizen

     │

     ▼

Report Service

     │

Publish Event

     │

     ▼

RabbitMQ

     │

     ▼

Email Notification Service

     │

     ▼

Email Sent

     │

     ▼

NotificationLog Saved

     │

     ▼

GET /api/notifications
```

The NotificationLog collection should contain the newly generated notification, and the GET APIs should return it successfully.

---

# Verification Checklist

| Verification | Status |
|--------------|--------|
| Application starts successfully | ✅ |
| Swagger UI accessible | ✅ |
| Eureka registration successful | ✅ |
| RabbitMQ queues created | ✅ |
| MongoDB connected | ✅ |
| GET /api/notifications returns 200 | ✅ |
| GET /api/notifications/{id} returns 200 | ✅ |
| Invalid ID returns 404 | ✅ |
| Notification logs stored in MongoDB | ✅ |
| Retry API available | ✅ |
| Ready for RabbitMQ integration testing | ✅ |

---
# Installation & Setup Guide

This section explains how to set up and run the **Email Notification Service** on a local development environment.

---

# Prerequisites

Before running the service, install the following software.

| Software | Recommended Version |
|-----------|---------------------|
| Java | JDK 21 |
| Maven | 3.9+ |
| MongoDB | 8.x |
| RabbitMQ | 4.x |
| Erlang | OTP 27 |
| Git | Latest |
| IDE | IntelliJ IDEA / Eclipse / STS / VS Code |

---

# Clone Repository

```bash
git clone <repository-url>
```

```bash
cd email-notification-service
```

---

# Project Build

Compile the project.

```bash
mvn clean install
```

If compilation is successful, Maven generates the `target` directory.

---

# Required Services

The following services must be running before starting the Email Notification Service.

| Service | Default Port |
|----------|--------------|
| Config Server | 8888 |
| Eureka Server | 8761 |
| MongoDB | 27017 |
| RabbitMQ | 5672 |
| RabbitMQ Management | 15672 |

---

# MongoDB Setup

Start MongoDB.

Verify connection.

```javascript
show dbs
```

No database is required to be created manually.

MongoDB automatically creates

```
notification_db
```

when the first document is inserted.

---

# RabbitMQ Installation

Install

- RabbitMQ 4.x
- Erlang OTP 27

> **Note:** RabbitMQ 4.x is compatible with Erlang OTP 27. Using newer OTP versions may cause startup issues.

Start RabbitMQ.

Enable Management Plugin.

```bash
rabbitmq-plugins enable rabbitmq_management
```

Verify

```
http://localhost:15672
```

Default Credentials

Username

```
guest
```

Password

```
guest
```

---

# RabbitMQ Verification

After startup verify

- Exchange created
- Dead Letter Exchange created
- Queues created
- Dead Letter Queues created
- Routing Keys
- Queue Bindings

Expected Queues

```
email.report.created.q

email.report.assigned.q

email.report.status.changed.q

email.report.resolved.q
```

Expected DLQs

```
email.report.created.dlq

email.report.assigned.dlq

email.report.status.changed.dlq

email.report.resolved.dlq
```

---

# Gmail SMTP Configuration

This service uses Gmail SMTP for sending emails.

Create environment variables on your machine.

Example

```
EMAIL_USERNAME=your-email@gmail.com

EMAIL_PASSWORD=your-app-password

EMAIL_FROM=your-email@gmail.com
```

Do **not** commit these values to GitHub.

Use a Gmail **App Password**, not your normal Gmail password.

---

# Environment Variables

The following variables are required.

| Variable | Description |
|----------|-------------|
| EMAIL_USERNAME | Gmail account used for sending emails |
| EMAIL_PASSWORD | Gmail App Password |
| EMAIL_FROM | Sender email address |

---

# Spring Mail Configuration

The application reads values from environment variables.

Example

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}

email:
  from: ${EMAIL_FROM}
```

---

# Config Server

Ensure the Config Server is running.

Verify

```
http://localhost:8888
```

The Email Notification Service loads its external configuration during startup.

---

# Eureka Server

Start Eureka Server.

Verify

```
http://localhost:8761
```

After the Email Notification Service starts, it should appear as

```
EMAIL-NOTIFICATION-SERVICE
```

with status

```
UP
```

---

# Application Startup Order

Start services in the following order.

1. MongoDB
2. RabbitMQ
3. Config Server
4. Eureka Server
5. Email Notification Service
6. Producer Services (e.g., Report Service)

---

# Running the Application

Using Maven

```bash
mvn spring-boot:run
```

Using IDE

Run

```
EmailNotificationServiceApplication.java
```

---

# Verify Application Startup

Check console logs.

Expected

```
Started EmailNotificationServiceApplication
```

Verify

- MongoDB Connected
- RabbitMQ Connected
- Eureka Registered
- Config Loaded
- Scheduler Started
- Swagger Initialized

---

# Swagger Verification

Open

```
http://localhost:8084/swagger-ui/index.html
```

Verify all notification APIs are listed.

---

# REST API Verification

Get all notifications

```
GET

http://localhost:8084/api/notifications
```

Expected

```
200 OK
```

---

Get notification by ID

```
GET

http://localhost:8084/api/notifications/{id}
```

Expected

```
200 OK
```

Invalid ID

Expected

```
404 Not Found
```

---

# MongoDB Verification

Open MongoDB Shell.

```javascript
use notification_db

show collections
```

Expected

```
notification_logs

failed_notifications

email_templates

processed_events
```

View notifications.

```javascript
db.notification_logs.find().pretty()
```

---

# RabbitMQ Verification

Open

```
http://localhost:15672
```

Verify

- Exchange exists
- Queues exist
- DLQs exist
- Bindings exist
- Consumers connected

---

# Email Verification

Once a producer service publishes an event.

Verify

- Email received
- NotificationLog created
- ProcessedEvent created

If email sending fails.

Verify

- FailedNotification created
- Retry Scheduler retries delivery
- NotificationLog created after successful retry

---

# Troubleshooting

## Application does not start

Check

- Java version
- Maven version
- Config Server availability
- Eureka availability

---

## MongoDB Connection Failed

Verify

- MongoDB service is running
- Port 27017 is available
- Connection URI is correct

---

## RabbitMQ Connection Failed

Verify

- RabbitMQ service is running
- Erlang OTP version is compatible
- Port 5672 is available

---

## RabbitMQ Management UI Not Opening

Enable plugin

```bash
rabbitmq-plugins enable rabbitmq_management
```

Restart RabbitMQ.

---

## Emails Not Sending

Check

- Gmail App Password
- SMTP username
- SMTP password
- Internet connectivity
- Environment variables

---

## Service Not Registered in Eureka

Verify

- Eureka Server is running
- `spring.application.name` is correct
- Eureka URL is correct

---

# Security Notes

- Never commit `.env` files.
- Never commit Gmail App Passwords.
- Never commit production credentials.
- Keep sensitive values in environment variables or Config Server.
- Add `.env` to `.gitignore`.

---

# Setup Verification Checklist

| Task | Status |
|------|--------|
| Java Installed | ✅ |
| Maven Installed | ✅ |
| MongoDB Running | ✅ |
| RabbitMQ Running | ✅ |
| RabbitMQ Management Enabled | ✅ |
| Config Server Running | ✅ |
| Eureka Server Running | ✅ |
| Email Notification Service Running | ✅ |
| Swagger Accessible | ✅ |
| MongoDB Connected | ✅ |
| RabbitMQ Connected | ✅ |
| GET APIs Working | ✅ |
| Ready for Integration Testing | ✅ |

---
# Integration Guide

The Email Notification Service is designed as an **event-driven consumer microservice**.

It does not expose APIs for creating notifications. Instead, it listens to RabbitMQ events published by producer microservices.

---

# Microservice Communication

```
                         KARTYAVYA MICROSERVICES

                        +----------------------+
                        |    Config Server     |
                        +----------+-----------+
                                   |
                                   |
                        +----------v-----------+
                        |    Eureka Server     |
                        +----------+-----------+
                                   |
     -------------------------------------------------------------------
     |                    |                    |                         |
     |                    |                    |                         |
+----v-----+      +-------v------+     +-------v------+       +---------v---------+
| Auth     |      | User Service |     | Report Service|      | Email Notification|
| Service  |      |              |     |               |      |     Service       |
+----------+      +--------------+     +-------+-------+      +-------------------+
                                               |
                                               |
                                        Publish Event
                                               |
                                               ▼
                                          RabbitMQ
                                               |
                                               ▼
                                 Email Notification Service
                                               |
                         +---------------------+----------------------+
                         |                                            |
                         ▼                                            ▼
                    Gmail SMTP                                   MongoDB
```

---

# Producer Services

The Email Notification Service expects events from producer services.

Currently, the primary producer is:

```
Report Service
```

Future producer services may include

- User Service
- Department Service
- AI Service
- Complaint Escalation Service

---

# Supported Events

| Event | Published By |
|--------|--------------|
| ReportCreatedEvent | Report Service |
| ReportAssignedEvent | Report Service |
| ReportStatusChangedEvent | Report Service |
| ReportResolvedEvent | Report Service |

---

# Integration Workflow

## Complaint Registration

```
Citizen

      │

      ▼

Frontend

      │

      ▼

API Gateway

      │

      ▼

Report Service

      │

Save Complaint

      │

Publish ReportCreatedEvent

      │

      ▼

RabbitMQ

      │

      ▼

ReportCreatedConsumer

      │

      ▼

NotificationService

      │

      ▼

Load Email Template

      │

      ▼

Generate Email

      │

      ▼

Send Email

      │

      ▼

NotificationLog
```

---

## Complaint Assignment

```
Officer Assigned

      │

      ▼

Report Service

      │

Publish ReportAssignedEvent

      │

      ▼

RabbitMQ

      │

      ▼

ReportAssignedConsumer

      │

      ▼

NotificationService

      │

      ▼

Email Sent
```

---

## Complaint Status Changed

```
Report Service

      │

Status Updated

      │

Publish Event

      │

RabbitMQ

      │

Status Consumer

      │

NotificationService

      │

Email Sent
```

---

## Complaint Resolved

```
Report Service

      │

Complaint Closed

      │

Publish Event

      │

RabbitMQ

      │

Resolved Consumer

      │

NotificationService

      │

Email Sent

      │

NotificationLog
```

---

# End-to-End Event Flow

```
Producer Service

        │

        ▼

RabbitMQ Exchange

        │

        ▼

Routing Key

        │

        ▼

Queue

        │

        ▼

Consumer

        │

        ▼

NotificationServiceImpl

        │

        ▼

TemplateService

        │

        ▼

EmailService

        │

        ▼

SMTP Server

        │

        ▼

Recipient

        │

        ▼

NotificationLogRepository

        │

        ▼

MongoDB
```

---

# Complete Processing Flow

```
Receive Event

        │

        ▼

Validate Event

        │

        ▼

Duplicate Check

        │

        ▼

Load Email Template

        │

        ▼

Replace Variables

        │

        ▼

Generate Subject

        │

        ▼

Generate HTML Body

        │

        ▼

Send Email

        │

        ▼

Save NotificationLog

        │

        ▼

Save ProcessedEvent

        │

        ▼

Completed
```

---

# Failure Flow

```
Receive Event

      │

      ▼

Send Email

      │

      ▼

SMTP Failure

      │

      ▼

FailedNotification

      │

      ▼

Retry Scheduler

      │

      ▼

Retry Email

      │

      ▼

Success

      │

      ▼

NotificationLog
```

---

# Post-Integration Verification

After integrating with the Report Service, verify the following scenarios.

## Scenario 1

Citizen registers a complaint.

Expected

- ReportCreatedEvent published
- Email received
- NotificationLog stored
- ProcessedEvent stored

---

## Scenario 2

Officer assigned.

Expected

- ReportAssignedEvent published
- Email received
- NotificationLog created

---

## Scenario 3

Complaint status updated.

Expected

- ReportStatusChangedEvent published
- Email received
- NotificationLog updated

---

## Scenario 4

Complaint resolved.

Expected

- ReportResolvedEvent published
- Resolution email delivered
- NotificationLog stored

---

# Deployment Readiness Checklist

Before deployment ensure the following.

## Infrastructure

- MongoDB running
- RabbitMQ running
- Config Server running
- Eureka Server running

---

## Configuration

- Environment variables configured
- Gmail App Password configured
- RabbitMQ connection verified
- MongoDB URI verified

---

## Application

- Application starts successfully
- Registered with Eureka
- Swagger accessible
- RabbitMQ consumers active
- Scheduler running

---

## Functional

- Templates available
- Emails sent successfully
- Retry mechanism verified
- Duplicate event prevention verified
- Notification logs created
- GET APIs tested

---

# Project Deliverables

The Email Notification Service includes:

- Event-driven notification processing
- RabbitMQ consumers
- MongoDB persistence
- Email template management
- Email delivery using Gmail SMTP
- Failed notification retry mechanism
- Notification history APIs
- Swagger documentation
- Global exception handling
- Idempotent event processing
- Eureka integration
- Spring Cloud Config integration

---

# Future Enhancements

The following features can be added in future releases.

### Notification Channels

- SMS
- Push Notifications
- WhatsApp
- In-App Notifications

---

### Template Management

- HTML templates
- Rich text editor
- Attachment support
- Multi-language templates

---

### Monitoring

- Delivery analytics
- Retry dashboard
- Email statistics
- Failure reports

---

### Scalability

- Kafka support
- Redis caching
- Horizontal scaling
- Distributed scheduling

---

# Conclusion

The Email Notification Service is a standalone, event-driven microservice responsible for delivering email notifications within the Kartyavya Civic Grievance Management System.

By consuming RabbitMQ events, processing dynamic email templates, ensuring idempotent event handling, retrying failed deliveries, and maintaining notification history in MongoDB, the service provides a scalable, reliable, and loosely coupled notification solution.

The service is fully prepared for integration with the remaining Kartyavya microservices and can be deployed independently while communicating asynchronously through RabbitMQ.

---

# Developed By

**Project:** Kartyavya – Civic Grievance Management System

**Microservice:** Email Notification Service

**Technology Stack:** Java 21, Spring Boot, Spring Cloud, RabbitMQ, MongoDB, Gmail SMTP, OpenAPI (Swagger), Maven

**Architecture:** Event-Driven Microservices
