package com.plantky.common.util;

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
     * <p>数据库中的 establishment 值可能保存为 {@code introduced}，
     * API 希望展示为 {@code Introduced}。</p>
     *
     * @param value 原始文本
     * @return 首字母大写后的文本；null/空白输入返回 null
     */
    public static String capitalizeFirst(String value) {
        // Spring StringUtils.hasText 会同时判断 null、"" 和纯空格字符串。
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
     * <p>LIKE 语义中：</p>
     * <ul>
     *     <li>{@code %} 表示任意长度字符；</li>
     *     <li>{@code _} 表示任意一个字符；</li>
     * </ul>
     *
     * <p>用户搜索植物名时，我们希望把用户输入视为普通文本，而不是让用户无意中改变 LIKE 语义，
     * 因此提前转义这些字符。MyBatis-Plus 仍通过参数绑定生成 SQL，不进行字符串拼接。</p>
     *
     * @param value 已经完成非空校验的搜索关键词
     * @return 转义后的 LIKE 关键词
     */
    public static String escapeLikeKeyword(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
