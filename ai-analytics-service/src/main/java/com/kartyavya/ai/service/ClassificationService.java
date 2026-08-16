package com.kartyavya.ai.service;

import com.kartyavya.contracts.ClassificationContracts;
import org.springframework.stereotype.Service;

@Service
public class ClassificationService {
	private final GeminiClient gemini;

	public ClassificationService(GeminiClient gemini) {
		this.gemini = gemini;
	}

	public ClassificationContracts.Response classify(ClassificationContracts.Request request) {
		return gemini.classify(request);
	}
}
