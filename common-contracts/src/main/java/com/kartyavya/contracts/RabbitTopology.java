package com.kartyavya.contracts;

public final class RabbitTopology {
	private RabbitTopology() {
	}

	public static final String EXCHANGE = "kartyavya.events";
	public static final String EMAIL_QUEUE = "kartyavya.email.q";
	public static final String ANALYTICS_QUEUE = "kartyavya.analytics.q";
	public static final String EMAIL_DLX = "kartyavya.email.dlx";
	public static final String EMAIL_DLQ = "kartyavya.email.dlq";
}
