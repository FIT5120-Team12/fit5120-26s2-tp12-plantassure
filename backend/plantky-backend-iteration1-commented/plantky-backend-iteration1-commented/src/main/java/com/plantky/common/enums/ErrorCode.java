package com.plantky.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 后端统一业务错误码。
 *
 * <p>每个错误同时绑定：</p>
 * <ul>
 *     <li>HTTP Status：符合 HTTP 语义，例如 400 / 404 / 500；</li>
 *     <li>业务错误 code：方便前端稳定识别错误类型；</li>
 *     <li>默认 message：没有特殊说明时直接使用。</li>
 * </ul>
 *
 * <p>把这些值集中管理，可以避免 Controller 或 Service 中散落大量
 * {@code "PLANT_NOT_FOUND"} 之类的魔法字符串。</p>
 */
@Getter
public enum ErrorCode {

    INVALID_SEARCH_QUERY(
            HttpStatus.BAD_REQUEST,
            "INVALID_SEARCH_QUERY",
            "Search query must not be blank."),

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "The request is invalid."),

    PLANT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLANT_NOT_FOUND",
            "Plant not found."),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "Unexpected backend error.");

    /** HTTP 状态码。 */
    private final HttpStatus httpStatus;

    /** 前端可用于程序判断的稳定错误编码。 */
    private final String code;

    /** 默认错误提示。 */
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
