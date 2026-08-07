package com.kartyavya.email.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private DateTimeUtil() {
	}

	public static LocalDateTime now() {
		return LocalDateTime.now();
	}

	public static String format(LocalDateTime dateTime) {

		if (dateTime == null) {
			return "";
		}

		return dateTime.format(FORMATTER);
	}
}