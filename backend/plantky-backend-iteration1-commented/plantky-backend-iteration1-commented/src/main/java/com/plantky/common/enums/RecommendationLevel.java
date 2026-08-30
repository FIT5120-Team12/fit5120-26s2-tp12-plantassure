package com.plantky.common.enums;

import lombok.Getter;

/**
 * 前端最终展示的 Recommendation 等级。
 *
 * <p>枚举常量用于程序内部判断，{@link #displayLabel} 用于用户界面展示。</p>
 *
 * <p>例如：</p>
 * <pre>
 * 程序内部：USE_CAUTION
 * 前端展示：Use Caution
 * </pre>
 */
@Getter
public enum RecommendationLevel {

    LOWER_CONCERN("Lower Concern"),
    USE_CAUTION("Use Caution"),
    RECONSIDER_PLANTING("Reconsider Planting"),
    NOT_ASSESSED("Not Assessed");

    /** 面向最终用户的英文展示文案。 */
    private final String displayLabel;

    RecommendationLevel(String displayLabel) {
        this.displayLabel = displayLabel;
    }
}
