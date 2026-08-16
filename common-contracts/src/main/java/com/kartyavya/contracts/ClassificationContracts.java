package com.kartyavya.contracts;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public final class ClassificationContracts {
	private ClassificationContracts() {
	}

	public record Request(@NotBlank String title, @NotBlank String description) {
	}

	public record Response(ReportCategory category, Severity severity, double confidence,
			String suggestedDepartmentCode, List<String> signals, String modelVersion) {
	}
}
