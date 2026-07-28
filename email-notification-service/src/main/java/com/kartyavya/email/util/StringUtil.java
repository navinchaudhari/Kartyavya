package com.kartyavya.email.util;

public final class StringUtil {

	private StringUtil() {
	}

	public static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public static boolean isBlank(String value) {
		return !hasText(value);
	}

	public static String defaultIfBlank(String value, String defaultValue) {

		return hasText(value) ? value : defaultValue;
	}
}