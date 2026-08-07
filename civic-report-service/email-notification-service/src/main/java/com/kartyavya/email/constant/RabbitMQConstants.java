package com.kartyavya.email.constant;

public final class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    /*
     * Exchanges
     */
    public static final String EXCHANGE = "kartyavya.events";
    public static final String DEAD_LETTER_EXCHANGE = "kartyavya.events.dlx";

    /*
     * Routing Keys
     */
    public static final String REPORT_CREATED = "report.created";
    public static final String REPORT_ASSIGNED = "report.assigned";
    public static final String REPORT_STATUS_CHANGED = "report.status.changed";
    public static final String REPORT_RESOLVED = "report.resolved";

    /*
     * Dead Letter Routing Keys
     */
    public static final String REPORT_CREATED_DLQ = "report.created.dlq";
    public static final String REPORT_ASSIGNED_DLQ = "report.assigned.dlq";
    public static final String REPORT_STATUS_CHANGED_DLQ = "report.status.changed.dlq";
    public static final String REPORT_RESOLVED_DLQ = "report.resolved.dlq";

    /*
     * Queues
     */
    public static final String REPORT_CREATED_QUEUE = "email.report.created.q";
    public static final String REPORT_ASSIGNED_QUEUE = "email.report.assigned.q";
    public static final String REPORT_STATUS_CHANGED_QUEUE = "email.report.status.changed.q";
    public static final String REPORT_RESOLVED_QUEUE = "email.report.resolved.q";

    /*
     * Dead Letter Queues
     */
    public static final String REPORT_CREATED_DL_QUEUE = "email.report.created.dlq";
    public static final String REPORT_ASSIGNED_DL_QUEUE = "email.report.assigned.dlq";
    public static final String REPORT_STATUS_CHANGED_DL_QUEUE = "email.report.status.changed.dlq";
    public static final String REPORT_RESOLVED_DL_QUEUE = "email.report.resolved.dlq";

}