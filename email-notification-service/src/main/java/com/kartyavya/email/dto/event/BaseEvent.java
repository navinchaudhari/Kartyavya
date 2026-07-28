package com.kartyavya.email.dto.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BaseEvent {

    private String eventId;

    private String correlationId;

    private LocalDateTime eventTime;

    private String reportId;

}