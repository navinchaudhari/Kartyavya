package com.kartyavya.email.service.impl;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.kartyavya.email.entity.EmailTemplate;
import com.kartyavya.email.exception.TemplateNotFoundException;
import com.kartyavya.email.repository.EmailTemplateRepository;
import com.kartyavya.email.service.TemplateService;
import com.kartyavya.email.util.EmailTemplateUtil;
import com.kartyavya.email.util.StringUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

	private final EmailTemplateRepository emailTemplateRepository;

	@Override
	public EmailTemplate getLatestTemplate(String templateKey) {

		if (!StringUtil.hasText(templateKey)) {
			throw new TemplateNotFoundException("Template key cannot be empty.");
		}

		return emailTemplateRepository.findTopByTemplateKeyOrderByVersionDesc(templateKey)
				.orElseThrow(() -> new TemplateNotFoundException("Template not found : " + templateKey));
	}

	@Override
	public String renderSubject(EmailTemplate template, Map<String, Object> variables) {

		Objects.requireNonNull(template, "EmailTemplate cannot be null.");

		Objects.requireNonNull(variables, "Variables cannot be null.");

		return EmailTemplateUtil.replaceVariables(template.getSubject(), variables);
	}

	@Override
	public String renderBody(EmailTemplate template, Map<String, Object> variables) {

		Objects.requireNonNull(template, "EmailTemplate cannot be null.");

		Objects.requireNonNull(variables, "Variables cannot be null.");

		return EmailTemplateUtil.replaceVariables(template.getBody(), variables);
	}
}