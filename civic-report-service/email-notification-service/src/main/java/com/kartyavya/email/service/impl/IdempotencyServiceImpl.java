package com.kartyavya.email.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kartyavya.email.entity.ProcessedEvent;
import com.kartyavya.email.repository.ProcessedEventRepository;
import com.kartyavya.email.service.IdempotencyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(IdempotencyServiceImpl.class);

	private final ProcessedEventRepository processedEventRepository;

	@Override
	public boolean isProcessed(String eventId) {

		boolean processed = processedEventRepository.existsByEventId(eventId);

		LOGGER.debug("Idempotency check for event {} : {}", eventId, processed);

		return processed;
	}

	@Override
	public void markProcessed(String eventId) {

		ProcessedEvent event = ProcessedEvent.builder().eventId(eventId).processedAt(LocalDateTime.now()).build();

		processedEventRepository.save(event);

		LOGGER.info("Event {} marked as processed.", eventId);
	}

}