package com.plantky.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Iteration 2 height range 展示规则测试。 */
class DisplayValueUtilsTest {

    @Test
    void shouldFormatHeightWithoutInventingMissingValues() {
        assertThat(DisplayValueUtils.formatHeightRange(null, null)).isNull();
        assertThat(DisplayValueUtils.formatHeightRange(0.3, 1.0)).isEqualTo("0.3–1 m");
        assertThat(DisplayValueUtils.formatHeightRange(1.0, 1.0)).isEqualTo("1 m");
    }
}
