package com.plantky.domain.vo.assessment;

import com.plantky.common.enums.EnvironmentalConcern;
import lombok.Builder;
import lombok.Getter;

/** Iteration 2 normalized environmental concern block. */
@Getter
@Builder
public class EnvironmentalConcernVO {
    private final EnvironmentalConcern status;
    private final String source;
}
