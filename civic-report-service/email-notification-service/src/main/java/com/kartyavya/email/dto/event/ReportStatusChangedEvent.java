package com.kartyavya.email.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReportStatusChangedEvent extends BaseEvent {

    private String citizenEmail;

    private String oldStatus;

    private String newStatus;

    private String remarks;

}