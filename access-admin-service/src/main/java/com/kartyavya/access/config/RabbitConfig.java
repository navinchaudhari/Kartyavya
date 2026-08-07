package com.kartyavya.access.config;

import com.kartyavya.contracts.RabbitTopology;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitConfig {
    @Bean
    TopicExchange eventExchange() {
        return ExchangeBuilder.topicExchange(RabbitTopology.EXCHANGE).durable(true).build();
    }

    @Bean
    Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
