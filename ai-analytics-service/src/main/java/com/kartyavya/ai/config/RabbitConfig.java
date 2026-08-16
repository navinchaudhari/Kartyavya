package com.kartyavya.ai.config;

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
	Queue analyticsQueue() {
		return QueueBuilder.durable(RabbitTopology.ANALYTICS_QUEUE).withArguments(Map.of("x-dead-letter-exchange", ""))
				.build();
	}

	@Bean
	Binding reportBinding(Queue analyticsQueue, TopicExchange exchange) {
		return BindingBuilder.bind(analyticsQueue).to(exchange).with("report.#");
	}

	@Bean
	Binding correctionBinding(Queue analyticsQueue, TopicExchange exchange) {
		return BindingBuilder.bind(analyticsQueue).to(exchange).with("classification.#");
	}
}
