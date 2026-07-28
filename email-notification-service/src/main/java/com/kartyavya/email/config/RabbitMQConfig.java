package com.kartyavya.email.config;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kartyavya.email.constant.RabbitMQConstants;

@Configuration
public class RabbitMQConfig {

	/*
	 * ------------------------- Message Converter -------------------------
	 */

	@Bean
	Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/*
	 * ------------------------- Rabbit Template -------------------------
	 */

	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter());

		return template;
	}

	/*
	 * ------------------------- Exchanges -------------------------
	 */

	@Bean
	TopicExchange exchange() {
		return new TopicExchange(RabbitMQConstants.EXCHANGE, true, false);
	}

	@Bean
	TopicExchange deadLetterExchange() {
		return new TopicExchange(RabbitMQConstants.DEAD_LETTER_EXCHANGE, true, false);
	}

	/*
	 * ------------------------- Queue Arguments -------------------------
	 */

	private Map<String, Object> queueArguments(String deadLetterRoutingKey) {

		Map<String, Object> args = new HashMap<>();

		args.put("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE);

		args.put("x-dead-letter-routing-key", deadLetterRoutingKey);

		return args;
	}

	/*
	 * ------------------------- Primary Queues -------------------------
	 */

	@Bean
	Queue reportCreatedQueue() {

		return QueueBuilder.durable(RabbitMQConstants.REPORT_CREATED_QUEUE)
				.withArguments(queueArguments(RabbitMQConstants.REPORT_CREATED_DLQ)).build();
	}

	@Bean
	Queue reportAssignedQueue() {

		return QueueBuilder.durable(RabbitMQConstants.REPORT_ASSIGNED_QUEUE)
				.withArguments(queueArguments(RabbitMQConstants.REPORT_ASSIGNED_DLQ)).build();
	}

	@Bean
	Queue reportStatusChangedQueue() {

		return QueueBuilder.durable(RabbitMQConstants.REPORT_STATUS_CHANGED_QUEUE)
				.withArguments(queueArguments(RabbitMQConstants.REPORT_STATUS_CHANGED_DLQ)).build();
	}

	@Bean
	Queue reportResolvedQueue() {

		return QueueBuilder.durable(RabbitMQConstants.REPORT_RESOLVED_QUEUE)
				.withArguments(queueArguments(RabbitMQConstants.REPORT_RESOLVED_DLQ)).build();
	}

	/*
	 * ------------------------- Dead Letter Queues -------------------------
	 */

	@Bean
	Queue reportCreatedDLQ() {
		return QueueBuilder.durable(RabbitMQConstants.REPORT_CREATED_DL_QUEUE).build();
	}

	@Bean
	Queue reportAssignedDLQ() {
		return QueueBuilder.durable(RabbitMQConstants.REPORT_ASSIGNED_DL_QUEUE).build();
	}

	@Bean
	Queue reportStatusChangedDLQ() {
		return QueueBuilder.durable(RabbitMQConstants.REPORT_STATUS_CHANGED_DL_QUEUE).build();
	}

	@Bean
	Queue reportResolvedDLQ() {
		return QueueBuilder.durable(RabbitMQConstants.REPORT_RESOLVED_DL_QUEUE).build();
	}

	/*
	 * ------------------------- Primary Bindings -------------------------
	 */

	@Bean
	Binding reportCreatedBinding() {
		return BindingBuilder.bind(reportCreatedQueue()).to(exchange()).with(RabbitMQConstants.REPORT_CREATED);
	}

	@Bean
	Binding reportAssignedBinding() {
		return BindingBuilder.bind(reportAssignedQueue()).to(exchange()).with(RabbitMQConstants.REPORT_ASSIGNED);
	}

	@Bean
	Binding reportStatusChangedBinding() {
		return BindingBuilder.bind(reportStatusChangedQueue()).to(exchange())
				.with(RabbitMQConstants.REPORT_STATUS_CHANGED);
	}

	@Bean
	Binding reportResolvedBinding() {
		return BindingBuilder.bind(reportResolvedQueue()).to(exchange()).with(RabbitMQConstants.REPORT_RESOLVED);
	}

	/*
	 * ------------------------- DLQ Bindings -------------------------
	 */

	@Bean
	Binding reportCreatedDLQBinding() {
		return BindingBuilder.bind(reportCreatedDLQ()).to(deadLetterExchange())
				.with(RabbitMQConstants.REPORT_CREATED_DLQ);
	}

	@Bean
	Binding reportAssignedDLQBinding() {
		return BindingBuilder.bind(reportAssignedDLQ()).to(deadLetterExchange())
				.with(RabbitMQConstants.REPORT_ASSIGNED_DLQ);
	}

	@Bean
	Binding reportStatusChangedDLQBinding() {
		return BindingBuilder.bind(reportStatusChangedDLQ()).to(deadLetterExchange())
				.with(RabbitMQConstants.REPORT_STATUS_CHANGED_DLQ);
	}

	@Bean
	Binding reportResolvedDLQBinding() {
		return BindingBuilder.bind(reportResolvedDLQ()).to(deadLetterExchange())
				.with(RabbitMQConstants.REPORT_RESOLVED_DLQ);
	}

	/*
	 * ------------------------- Retry Configuration -------------------------
	 */

	@Bean
	public Advice retryInterceptor() {

		return RetryInterceptorBuilder.stateless()

				.maxAttempts(3)

				.backOffOptions(2000, 2.0, 10000)

	            .recoverer(new RejectAndDontRequeueRecoverer())

				.build();
	}

	/*
	 * ------------------------- Listener Container -------------------------
	 */

	@Bean
	SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

		factory.setConnectionFactory(connectionFactory);

		factory.setMessageConverter(messageConverter());

		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

		factory.setAdviceChain(retryInterceptor());

		factory.setDefaultRequeueRejected(false);

		return factory;
	}

}