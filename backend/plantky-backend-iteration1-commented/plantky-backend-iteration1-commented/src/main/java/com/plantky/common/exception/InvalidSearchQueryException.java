package com.plantky.common.exception;

import com.plantky.common.enums.ErrorCode;

/**
 * 植物搜索参数不合法异常。
 *
 * <p>典型触发场景：</p>
 * <ul>
 *     <li>{@code q} 为空字符串；</li>
 *     <li>{@code q} 只包含空格；</li>
 *     <li>{@code q} 超过后端允许的最大长度。</li>
 * </ul>
 *
 * <p>最终由 GlobalExceptionHandler 转换为 HTTP 400。</p>
 */
public class InvalidSearchQueryException extends BusinessException {

    /** 使用 INVALID_SEARCH_QUERY 的默认错误消息。 */
    public InvalidSearchQueryException() {
        super(ErrorCode.INVALID_SEARCH_QUERY);
    }

    /**
     * 使用更具体的错误消息，例如 “Search query must not exceed 255 characters.”。
     *
     * @param message 具体错误提示
     */
    public InvalidSearchQueryException(String message) {
        super(ErrorCode.INVALID_SEARCH_QUERY, message);
    }
}
