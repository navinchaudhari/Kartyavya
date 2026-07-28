package com.kartyavya.email.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kartyavya.email.entity.ProcessedEvent;

// is used for idempotency checks using eventId
@Repository
public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {

	boolean existsByEventId(String eventId); // This will be used inside every RabbitMQ consumer.

	Optional<ProcessedEvent> findByEventId(String eventId);

}