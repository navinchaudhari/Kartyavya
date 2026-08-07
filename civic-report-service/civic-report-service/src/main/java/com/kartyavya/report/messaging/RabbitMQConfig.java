package com.kartyavya.report.messaging;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "kartyavya.events";

    public static final String REPORT_CREATED_QUEUE =
            "email.report.created.q";

    public static final String REPORT_CREATED_ROUTING_KEY =
            "report.created";


    @Bean
    public DirectExchange reportExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }


    @Bean
    public Queue reportCreatedQueue() {
        return new Queue(REPORT_CREATED_QUEUE, true);
    }


    @Bean
    public Binding reportCreatedBinding() {

        return BindingBuilder
                .bind(reportCreatedQueue())
                .to(reportExchange())
                .with(REPORT_CREATED_ROUTING_KEY);
    }


    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {

        RabbitAdmin admin = new RabbitAdmin(connectionFactory);

        admin.declareExchange(reportExchange());
        System.out.println("Exchange declared: " + EXCHANGE_NAME);

        admin.declareQueue(reportCreatedQueue());
        System.out.println("Queue declared: " + REPORT_CREATED_QUEUE);

        admin.declareBinding(reportCreatedBinding());
        System.out.println("Binding declared: " + REPORT_CREATED_ROUTING_KEY);

        return admin;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


   
    
    @PostConstruct
    public void init() {
        System.out.println("RabbitMQConfig Loaded");
    }
}