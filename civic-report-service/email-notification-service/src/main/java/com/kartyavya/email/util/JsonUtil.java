package com.kartyavya.email.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JsonUtil() {
	}

	public static String toJson(Object object) {

		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Failed to convert object to JSON.", ex);
		}
	}

	public static <T> T fromJson(String json, Class<T> clazz) {

		try {
			return OBJECT_MAPPER.readValue(json, clazz);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to convert JSON to object.", ex);
		}
	}

	public static ObjectMapper mapper() {
		return OBJECT_MAPPER;
	}
}