package com.kartyavya.email.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.kartyavya.email.dto.event.ReportResolvedEvent;
import com.kartyavya.email.service.NotificationService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportResolvedConsumer extends AbstractNotificationConsumer {

	private final NotificationService notificationService;

	@RabbitListener(queues = "${rabbitmq.queue.report-resolved}", containerFactory = "rabbitListenerContainerFactory")
	public void consume(ReportResolvedEvent event, Channel channel, Message message) throws Exception {

		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		logger.info("Received REPORT_RESOLVED Event. EventId={}", event.getEventId());

		try {

			notificationService.processReportResolved(event);

			acknowledge(channel, deliveryTag);

			logger.info("REPORT_RESOLVED Event processed successfully. EventId={}", event.getEventId());

		} catch (Exception ex) {

			logger.error("Failed processing REPORT_RESOLVED Event. EventId={}", event.getEventId(), ex);

			throw ex;
		}
	}
}