package com.kartyavya.email.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReportCreatedEvent extends BaseEvent {

    private String citizenId;

    private String citizenName;

    private String citizenEmail;

    private String category;

    private String title;

}