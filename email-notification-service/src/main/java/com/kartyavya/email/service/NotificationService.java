package com.kartyavya.email.service;

import com.kartyavya.email.dto.event.ReportAssignedEvent;
import com.kartyavya.email.dto.event.ReportCreatedEvent;
import com.kartyavya.email.dto.event.ReportResolvedEvent;
import com.kartyavya.email.dto.event.ReportStatusChangedEvent;

public interface NotificationService {

	/**
	 * Process REPORT_CREATED event.
	 *
	 * @param event RabbitMQ Event
	 */
	void processReportCreated(ReportCreatedEvent event);
	
	void processReportAssigned(ReportAssignedEvent event);
	
	void processReportStatusChanged(ReportStatusChangedEvent event);
	
	void processReportResolved(ReportResolvedEvent event);

}