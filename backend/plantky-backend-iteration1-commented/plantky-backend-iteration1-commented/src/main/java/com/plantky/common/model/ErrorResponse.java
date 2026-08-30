package com.plantky.common.model;

import lombok.Builder;
import lombok.Getter;

/**
 * API 统一错误响应模型。
 *
 * <p>正常成功响应遵循当前 Iteration 1 API Contract，不额外包装 code/msg/data；
 * 但是异常响应使用统一结构，方便 Vue/Apifox 稳定处理错误。</p>
 *
 * <pre>
 * {
 *   "code": "PLANT_NOT_FOUND",
 *   "message": "Plant not found.",
 *   "path": "/api/v1/plants/999999/assessment"
 * }
 * </pre>
 *
 * <p>{@code @Getter} 自动生成 getter；{@code @Builder} 自动生成 builder() 构建器。</p>
 */
@Getter
@Builder
public class ErrorResponse {

    /** 业务错误码，适合前端逻辑判断，不应该依赖 message 文案。 */
    private final String code;

    /** 给调用方阅读的错误信息。 */
    private final String message;

    /** 发生错误的请求路径，用于排查问题。 */
    private final String path;
}
