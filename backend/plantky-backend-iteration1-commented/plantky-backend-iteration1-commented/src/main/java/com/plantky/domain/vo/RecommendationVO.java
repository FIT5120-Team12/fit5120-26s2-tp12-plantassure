package com.plantky.domain.vo;

import com.plantky.common.enums.RecommendationLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * Assessment API 最终 Recommendation 区域。
 */
@Getter
@Builder
public class RecommendationVO {

    /** 程序使用的标准枚举值，例如 USE_CAUTION。 */
    private final RecommendationLevel level;

    /** 用户界面展示值，例如 Use Caution。 */
    private final String displayLabel;

    /**
     * 自然语言解释。
     * 解释可以引用 establishment、occurrence、risk 等实际证据，
     * 但 Recommendation 的等级只由已验证的 environmental risk 决定。
     */
    private final String explanation;
}
