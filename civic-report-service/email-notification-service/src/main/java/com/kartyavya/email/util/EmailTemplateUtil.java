package com.kartyavya.email.util;

import java.util.Map;

public final class EmailTemplateUtil {

	private EmailTemplateUtil() {
	}

	public static String replaceVariables(String content, Map<String, Object> variables) {

		if (content == null) {
			return "";
		}

		String result = content;

		for (Map.Entry<String, Object> entry : variables.entrySet()) {

			String placeholder = "{{" + entry.getKey() + "}}";

			String value = entry.getValue() == null ? "" : entry.getValue().toString();

			result = result.replace(placeholder, value);
		}

		return result;
	}
}