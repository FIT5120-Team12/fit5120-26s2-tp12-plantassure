package com.plantky.domain.vo;

import com.plantky.common.enums.RiskAssessmentStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Assessment API 的 Environmental Risk 区域响应对象。
 */
@Getter
@Builder
public class EnvironmentalRiskVO {

    /**
     * 当前风险检查状态：ASSESSED / NOT_ASSESSED / UNAVAILABLE。
     *
     * <p>前端应该优先依据该字段判断如何展示，而不是通过 rating 是否为 null 猜测状态。</p>
     */
    private final RiskAssessmentStatus assessmentStatus;

    /**
     * 规范化后的风险等级，例如 High / Moderately High。
     * NOT_ASSESSED 或 UNAVAILABLE 时为 null。
     */
    private final String rating;

    /** 面向用户的一句话自然语言解释。 */
    private final String explanation;

    /** 风险信息来源，当前固定为 2022 Advisory List。 */
    private final String source;
}
