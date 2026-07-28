package com.kartyavya.email.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kartyavya.email.entity.EmailTemplate;
/*
 * Stores dynamic email templates.
 * Retrieve versioned email templates
 */
@Repository
public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String> {

	Optional<EmailTemplate> findByTemplateKeyAndVersion(String templateKey, Integer version);

	Optional<EmailTemplate> findTopByTemplateKeyOrderByVersionDesc(String templateKey);  // always returns the latest template version.

}