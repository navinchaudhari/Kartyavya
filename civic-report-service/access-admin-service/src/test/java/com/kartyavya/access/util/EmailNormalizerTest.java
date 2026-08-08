package com.kartyavya.access.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EmailNormalizerTest {

    @Test
    void normalize_null_returnsNull() {
        assertThat(EmailNormalizer.normalize(null)).isNull();
    }

    @Test
    void normalize_blank_returnsBlank() {
        assertThat(EmailNormalizer.normalize("   ")).isEmpty();
    }

    @Test
    void normalize_mixedCaseWithSpaces_returnsTrimmedLowerCase() {
        assertThat(EmailNormalizer.normalize("  USER@Example.COM  ")).isEqualTo("user@example.com");
    }

    @Test
    void normalize_alreadyNormalized_returnsUnchanged() {
        assertThat(EmailNormalizer.normalize("test@kartyavya.local")).isEqualTo("test@kartyavya.local");
    }
}
