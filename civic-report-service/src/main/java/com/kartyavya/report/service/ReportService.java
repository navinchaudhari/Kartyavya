package com.kartyavya.report.service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kartyavya.report.dto.ReportRequestDTO;
import com.kartyavya.report.dto.ReportResponseDTO;
import com.kartyavya.report.entity.Report;
import com.kartyavya.report.event.ReportCreatedEvent;
import com.kartyavya.report.exception.ReportNotFoundException;
import com.kartyavya.report.mapper.ReportMapper;
import com.kartyavya.report.messaging.ReportEventPublisher;
import com.kartyavya.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository repository;

	private final ReportEventPublisher eventPublisher;

	// CREATE
	public ReportResponseDTO createReport(ReportRequestDTO request) {

		Report report = ReportMapper.toEntity(request);

		Report savedReport = repository.save(report);

		// Create RabbitMQ REPORT_CREATED Event

		ReportCreatedEvent event = ReportCreatedEvent.builder()

				.eventId(UUID.randomUUID().toString())

				.correlationId(UUID.randomUUID().toString())

				.eventTime(LocalDateTime.now())

				.reportId(String.valueOf(savedReport.getId()))

				.category(savedReport.getCategory())

				.title(savedReport.getTitle())

				.build();

		// Publish Event to RabbitMQ

		eventPublisher.publishReportCreated(event);

		return ReportMapper.toResponseDTO(savedReport);
	}

	// GET ALL
	public List<ReportResponseDTO> getAllReports() {

		return repository.findAll().stream().map(ReportMapper::toResponseDTO).toList();
	}

	// GET BY ID
	public ReportResponseDTO getReportById(Long id) {

		Report report = repository.findById(id)
				.orElseThrow(() -> new ReportNotFoundException("Report not found : " + id));

		return ReportMapper.toResponseDTO(report);
	}

	// UPDATE
	public ReportResponseDTO updateReport(Long id, ReportRequestDTO request) {

		Report existing = repository.findById(id)
				.orElseThrow(() -> new ReportNotFoundException("Report not found : " + id));

		existing.setTitle(request.getTitle());

		existing.setDescription(request.getDescription());

		existing.setCategory(request.getCategory());

		existing.setSeverity(request.getSeverity());

		existing.setLatitude(request.getLatitude());

		existing.setLongitude(request.getLongitude());

		Report updated = repository.save(existing);

		return ReportMapper.toResponseDTO(updated);

	}

	// DELETE
	public void deleteReport(Long id) {

		Report report = repository.findById(id)
				.orElseThrow(() -> new ReportNotFoundException("Report not found : " + id));

		repository.delete(report);

	}

}