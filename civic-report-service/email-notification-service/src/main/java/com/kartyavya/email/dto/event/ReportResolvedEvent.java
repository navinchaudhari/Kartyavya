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
public class ReportResolvedEvent extends BaseEvent {

    private String citizenEmail;

    private String resolutionRemarks;

    private LocalDateTime resolvedAt;

}