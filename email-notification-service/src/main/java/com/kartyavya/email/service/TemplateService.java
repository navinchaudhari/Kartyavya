package com.kartyavya.email.service;

import java.util.Map;

import com.kartyavya.email.entity.EmailTemplate;

public interface TemplateService {

	/**
	 * Returns the latest version of the given template.
	 *
	 * @param templateKey Template key
	 * @return EmailTemplate
	 */
	EmailTemplate getLatestTemplate(String templateKey);

	/**
	 * Renders subject after replacing placeholders.
	 *
	 * @param template  Email template
	 * @param variables Placeholder values
	 * @return Rendered subject
	 */
	String renderSubject(EmailTemplate template, Map<String, Object> variables);

	/**
	 * Renders HTML body after replacing placeholders.
	 *
	 * @param template  Email template
	 * @param variables Placeholder values
	 * @return Rendered HTML body
	 */
	String renderBody(EmailTemplate template, Map<String, Object> variables);

}