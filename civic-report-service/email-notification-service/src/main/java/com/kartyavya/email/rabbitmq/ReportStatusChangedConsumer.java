package com.kartyavya.email.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.kartyavya.email.dto.event.ReportStatusChangedEvent;
import com.kartyavya.email.service.NotificationService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportStatusChangedConsumer extends AbstractNotificationConsumer {

	private final NotificationService notificationService;

	@RabbitListener(queues = "${rabbitmq.queue.report-status-changed}", containerFactory = "rabbitListenerContainerFactory")
	public void consume(ReportStatusChangedEvent event, Channel channel, Message message) throws Exception {

		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		logger.info("Received REPORT_STATUS_CHANGED Event. EventId={}", event.getEventId());

		try {

			notificationService.processReportStatusChanged(event);

			acknowledge(channel, deliveryTag);

			logger.info("REPORT_STATUS_CHANGED Event processed successfully. EventId={}", event.getEventId());

		} catch (Exception ex) {

			logger.error("Failed processing REPORT_STATUS_CHANGED Event. EventId={}", event.getEventId(), ex);

			throw ex;
		}
	}
}