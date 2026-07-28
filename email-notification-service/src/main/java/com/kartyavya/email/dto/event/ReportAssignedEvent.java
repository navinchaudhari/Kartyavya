package com.kartyavya.email.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReportAssignedEvent extends BaseEvent {

    private String officerId;

    private String officerName;

    private String officerEmail;

    private String departmentName;

}