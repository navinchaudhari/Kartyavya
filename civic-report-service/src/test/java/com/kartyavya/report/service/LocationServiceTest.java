package com.kartyavya.report.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationServiceTest {
    private LocationService configured() {
        var service = new LocationService();
        ReflectionTestUtils.setField(service, "minLat", 18.0);
        ReflectionTestUtils.setField(service, "maxLat", 22.5);
        ReflectionTestUtils.setField(service, "minLng", 72.0);
        ReflectionTestUtils.setField(service, "maxLng", 80.5);
        return service;
    }

    @Test
    void acceptsCoordinatesInsideConfiguredArea() {
        configured().verify(21.05, 75.77);
    }

    @Test
    void rejectsCoordinatesOutsideConfiguredArea() {
        assertThatThrownBy(() -> configured().verify(28.61, 77.20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside");
    }

    @Test
    void computesDistanceInKilometres() {
        assertThat(configured().distance(21.05, 75.77, 21.06, 75.77)).isBetween(1.0, 1.2);
    }
}
