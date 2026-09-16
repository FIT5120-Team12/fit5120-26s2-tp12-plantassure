package com.plantky.common.util;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

/**
 * 与 API 展示值/搜索文本处理相关的无状态工具方法。
 *
 * <p>这是纯工具类，不需要交给 Spring 容器管理，因此没有 {@code @Component}。</p>
 */
public final class DisplayValueUtils {

    /** 禁止实例化工具类。 */
    private DisplayValueUtils() {
    }

    /**
     * 将字符串首字母转换为大写，并去除首尾空格。
     *
     * @param value 原始文本
     * @return 首字母大写后的文本；null/空白输入返回 null
     */
    public static String capitalizeFirst(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase();
        }

        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    /**
     * 转义 SQL LIKE 查询中的特殊字符。
     *
     * @param value 已经完成非空校验的搜索关键词
     * @return 转义后的 LIKE 关键词
     */
    public static String escapeLikeKeyword(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * Iteration 2：把 height_min / height_max 转换为统一展示文本。
     *
     * <p>规则只负责格式化真实数据，不推断缺失值：</p>
     * <ul>
     *     <li>min/max 都缺失 -> null</li>
     *     <li>只有一个值 -> "0.5 m"</li>
     *     <li>两者相同 -> "1 m"</li>
     *     <li>两者不同 -> "0.3–1 m"</li>
     * </ul>
     */
    public static String formatHeightRange(Double min, Double max) {
        if (min == null && max == null) {
            return null;
        }
        if (min == null) {
            return formatNumber(max) + " m";
        }
        if (max == null) {
            return formatNumber(min) + " m";
        }
        if (Double.compare(min, max) == 0) {
            return formatNumber(min) + " m";
        }
        return formatNumber(min) + "–" + formatNumber(max) + " m";
    }

    /** 使用 BigDecimal 去除无意义的尾随 0，避免展示 1.0 m / 0.300000 m。 */
    private static String formatNumber(Double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
