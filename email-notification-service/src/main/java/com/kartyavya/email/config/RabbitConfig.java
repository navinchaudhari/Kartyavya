package com.kartyavya.email.config;

import com.kartyavya.contracts.RabbitTopology;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;
import java.util.Map;

@Configuration
public class RabbitConfig {
	@Bean
	TopicExchange exchange() {
		return ExchangeBuilder.topicExchange(RabbitTopology.EXCHANGE).durable(true).build();
	}

	@Bean
	DirectExchange dlx() {
		return ExchangeBuilder.directExchange(RabbitTopology.EMAIL_DLX).durable(true).build();
	}

	@Bean
	Queue emailQueue() {
		return QueueBuilder.durable(RabbitTopology.EMAIL_QUEUE)
				.withArguments(
						Map.of("x-dead-letter-exchange", RabbitTopology.EMAIL_DLX, "x-dead-letter-routing-key", "dead"))
				.build();
	}

	@Bean
	Queue dlq() {
		return QueueBuilder.durable(RabbitTopology.EMAIL_DLQ).build();
	}

	@Bean
	Binding all(Queue emailQueue, TopicExchange exchange) {
		return BindingBuilder.bind(emailQueue).to(exchange).with("#");
	}

	@Bean
	Binding dead(Queue dlq, DirectExchange dlx) {
		return BindingBuilder.bind(dlq).to(dlx).with("dead");
	}
}
