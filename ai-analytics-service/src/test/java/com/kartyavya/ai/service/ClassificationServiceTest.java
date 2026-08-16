package com.kartyavya.ai.service;

import com.kartyavya.contracts.ClassificationContracts;
import com.kartyavya.contracts.ReportCategory;
import com.kartyavya.contracts.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassificationServiceTest {
    @Test
    void delegatesClassificationToGeminiClientWithoutKeywordRules() {
        GeminiClient gemini = mock(GeminiClient.class);
        ClassificationContracts.Request request = new ClassificationContracts.Request(
                "Major pipeline burst near hospital",
                "Urgent flooding is creating danger for patients"
        );
        ClassificationContracts.Response expected = new ClassificationContracts.Response(
                ReportCategory.WATER_LEAKAGE,
                Severity.HIGH,
                94.5,
                "WATER_DRAINAGE",
                List.of("pipeline burst", "hospital access risk"),
                "gemini:gemini-2.5-flash-lite"
        );
        when(gemini.classify(request)).thenReturn(expected);

        ClassificationService service = new ClassificationService(gemini);
        ClassificationContracts.Response actual = service.classify(request);

        assertThat(actual).isEqualTo(expected);
        verify(gemini).classify(request);
    }
}
