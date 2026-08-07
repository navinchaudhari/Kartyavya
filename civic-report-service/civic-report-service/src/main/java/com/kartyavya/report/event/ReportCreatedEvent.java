package com.kartyavya.report.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReportCreatedEvent {

    private String eventId;

    private String correlationId;

    private LocalDateTime eventTime;

    private String reportId;

    private String citizenId;

    private String citizenName;

    private String citizenEmail;

    private String category;

    private String title;
}