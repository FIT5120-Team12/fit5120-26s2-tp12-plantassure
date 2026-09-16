package com.plantky.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Iteration 2 environmental concern 排序与 Not Assessed 语义测试。 */
class EnvironmentalConcernTest {

    @Test
    void shouldNeverTreatNotAssessedAsLowerConcern() {
        assertThat(EnvironmentalConcern.NOT_ASSESSED.isLowerThan(EnvironmentalConcern.HIGH)).isFalse();
        assertThat(EnvironmentalConcern.UNAVAILABLE.isLowerThan(EnvironmentalConcern.HIGH)).isFalse();
    }

    @Test
    void shouldRecogniseDocumentedLowerConcern() {
        assertThat(EnvironmentalConcern.MEDIUM.isLowerThan(EnvironmentalConcern.HIGH)).isTrue();
        assertThat(EnvironmentalConcern.LOWER.isLowerThan(EnvironmentalConcern.MEDIUM)).isTrue();
        assertThat(EnvironmentalConcern.HIGH.isLowerThan(EnvironmentalConcern.MEDIUM)).isFalse();
    }

    @Test
    void shouldMapIteration2NotAssessedMarker() {
        assertThat(EnvironmentalConcern.fromDatabaseValue("Not Assessed / No exact match"))
                .isEqualTo(EnvironmentalConcern.NOT_ASSESSED);
    }
}
