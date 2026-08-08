package com.kartyavya.access.validation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AtLeastOneFieldPresentValidatorTest {

    private final AtLeastOneFieldPresentValidator validator = new AtLeastOneFieldPresentValidator();

    record TestRecord(String fieldA, Long fieldB) {}

    static class TestClass {
        String fieldA;
        Long fieldB;
        TestClass(String a, Long b) { fieldA = a; fieldB = b; }
    }

    @Test
    void isValid_allNullRecord_returnsFalse() {
        assertThat(validator.isValid(new TestRecord(null, null), null)).isFalse();
    }

    @Test
    void isValid_oneNonNullRecord_returnsTrue() {
        assertThat(validator.isValid(new TestRecord("test", null), null)).isTrue();
    }

    @Test
    void isValid_allNonNullRecord_returnsTrue() {
        assertThat(validator.isValid(new TestRecord("test", 1L), null)).isTrue();
    }

    @Test
    void isValid_allNullClass_returnsFalse() {
        assertThat(validator.isValid(new TestClass(null, null), null)).isFalse();
    }

    @Test
    void isValid_oneNonNullClass_returnsTrue() {
        assertThat(validator.isValid(new TestClass("test", null), null)).isTrue();
    }

    @Test
    void isValid_nullObject_returnsTrue() {
        // null is handled by @NotNull if required
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
