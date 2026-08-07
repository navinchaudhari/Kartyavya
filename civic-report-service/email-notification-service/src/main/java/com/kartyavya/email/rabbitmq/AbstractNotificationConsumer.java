package com.kartyavya.email.rabbitmq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public abstract class AbstractNotificationConsumer {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected void acknowledge(Channel channel, long deliveryTag) throws IOException {

		channel.basicAck(deliveryTag, false);

		logger.info("Message acknowledged. DeliveryTag={}", deliveryTag);
	}

}