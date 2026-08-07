package com.kartyavya.access.service;

import com.kartyavya.contracts.*;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {
	private final RabbitTemplate rabbit;

	public void publish(String routing, String correlation, Object data) {
		rabbit.convertAndSend(RabbitTopology.EXCHANGE, routing, IntegrationEvent.of(routing, correlation, data));
	}
}
