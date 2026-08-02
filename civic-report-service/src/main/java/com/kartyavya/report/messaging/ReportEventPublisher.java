package com.kartyavya.report.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.kartyavya.report.event.ReportCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportEventPublisher {


    private final RabbitTemplate rabbitTemplate;


    public void publishReportCreated(ReportCreatedEvent event) {


        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                "report.created",
                event
        );

    }

}