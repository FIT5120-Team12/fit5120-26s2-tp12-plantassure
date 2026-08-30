package com.plantky.common.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

/**
 * Environmental Weed Risk 风险等级枚举。
 *
 * <p>这个枚举承担“数据库值”和“API 展示值”之间的转换职责，
 * 避免业务代码中到处出现字符串比较。</p>
 *
 * <p>示例：</p>
 * <pre>
 * 数据库保存："Moderately High Risk"
 * API 返回： "Moderately High"
 * 程序内部： MODERATELY_HIGH
 * </pre>
 */
@Getter
public enum EnvironmentalRiskRating {

    VERY_HIGH("Very High Risk", "Very High"),
    HIGH("High Risk", "High"),
    MODERATELY_HIGH("Moderately High Risk", "Moderately High"),
    MEDIUM("Medium Risk", "Medium"),
    LOWER("Lower Risk", "Lower");

    /** 数据库 species_data.risk_rating 中保存的标准文本。 */
    private final String databaseValue;

    /** API 返回给前端时使用的风险文本。 */
    private final String apiValue;

    EnvironmentalRiskRating(String databaseValue, String apiValue) {
        this.databaseValue = databaseValue;
        this.apiValue = apiValue;
    }

    /**
     * 根据数据库值解析对应枚举。
     *
     * <p>使用 {@link Optional} 而不是直接返回 null，明确告诉调用方：
     * “这个值可能无法匹配”。调用方必须显式处理无法匹配的情况。</p>
     *
     * @param databaseValue 数据库中的 risk_rating 原始值
     * @return 匹配成功返回对应枚举，否则返回 {@link Optional#empty()}
     */
    public static Optional<EnvironmentalRiskRating> fromDatabaseValue(String databaseValue) {
        if (databaseValue == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                // trim() 避免数据库文本两侧意外存在空格。
                // equalsIgnoreCase() 避免大小写差异导致合法值无法识别。
                .filter(item -> item.databaseValue.equalsIgnoreCase(databaseValue.trim()))
                .findFirst();
    }

    /**
     * 根据 API 风险文本反向解析枚举。
     *
     * <p>RecommendationService 使用该方法，把 RiskAssessmentService
     * 产生的 API rating 映射回业务枚举，再执行明确的 recommendation 规则。</p>
     *
     * @param apiValue API 风险文本，例如 "High"
     * @return 匹配到的风险枚举；无法匹配时返回 empty
     */
    public static Optional<EnvironmentalRiskRating> fromApiValue(String apiValue) {
        if (apiValue == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(item -> item.apiValue.equalsIgnoreCase(apiValue.trim()))
                .findFirst();
    }
}
