package com.plantky.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lombok.Getter;

/**
 * Iteration 2 对外统一使用的 environmental concern 枚举。
 *
 * <p>该枚举与 Iteration 1 的 {@link EnvironmentalRiskRating} 并存：
 * 旧前端仍然使用 EnvironmentalRiskVO 中的展示文本，新 Catalog / Alternatives / Compare
 * API 则使用稳定的 uppercase snake case 枚举值。</p>
 *
 * <p><strong>重要：</strong>NOT_ASSESSED 和 UNAVAILABLE 不参与“更低风险”排序，
 * 绝不能被当作 LOWER。</p>
 */
@Getter
public enum EnvironmentalConcern {

    VERY_HIGH("Very High Risk", 5),
    HIGH("High Risk", 4),
    MODERATELY_HIGH("Moderately High Risk", 3),
    MEDIUM("Medium Risk", 2),
    LOWER("Lower Risk", 1),
    NOT_ASSESSED(null, null),
    UNAVAILABLE(null, null);

    /** 数据库中受支持的 2022 Advisory List 文本。 */
    private final String databaseValue;

    /** 仅 documented concern 使用的严重程度；数值越大表示 concern 越高。 */
    private final Integer severity;

    EnvironmentalConcern(String databaseValue, Integer severity) {
        this.databaseValue = databaseValue;
        this.severity = severity;
    }

    /**
     * 把数据库 risk_rating 转换为受控 concern。
     *
     * <p>Iteration 2 数据显式保存 "Not Assessed / No exact match"，因此该文本
     * 与 null/空值都映射为 NOT_ASSESSED，而不是 UNAVAILABLE。</p>
     */
    public static EnvironmentalConcern fromDatabaseValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return NOT_ASSESSED;
        }

        String normalized = rawValue.trim();
        if ("Not Assessed / No exact match".equalsIgnoreCase(normalized)
                || "Not Assessed".equalsIgnoreCase(normalized)) {
            return NOT_ASSESSED;
        }

        return Arrays.stream(values())
                .filter(EnvironmentalConcern::isDocumented)
                .filter(item -> item.databaseValue.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(UNAVAILABLE);
    }

    /** 是否属于有正式 documented concern 的五个等级。 */
    public boolean isDocumented() {
        return severity != null;
    }

    /**
     * 判断 candidate concern 是否比 current concern 更低。
     * NOT_ASSESSED / UNAVAILABLE 自动返回 false。
     */
    public boolean isLowerThan(EnvironmentalConcern current) {
        return current != null
                && isDocumented()
                && current.isDocumented()
                && severity < current.severity;
    }

    /**
     * 返回比当前 concern 更低的数据库原始文本，供 Mapper 查询 alternatives 使用。
     */
    public static List<String> lowerDatabaseValuesThan(EnvironmentalConcern current) {
        if (current == null || !current.isDocumented()) {
            return List.of();
        }

        return Arrays.stream(values())
                .filter(EnvironmentalConcern::isDocumented)
                .filter(item -> item.severity < current.severity)
                .map(EnvironmentalConcern::getDatabaseValue)
                .toList();
    }

    /** 根据 API 枚举名称安全解析；主要用于测试与未来扩展。 */
    public static Optional<EnvironmentalConcern> fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
