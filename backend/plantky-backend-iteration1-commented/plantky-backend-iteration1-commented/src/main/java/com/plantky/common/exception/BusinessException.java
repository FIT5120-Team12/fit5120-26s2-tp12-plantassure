package com.plantky.common.exception;

import com.plantky.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 所有“可预期业务异常”的父类。
 *
 * <p>它继承 {@link RuntimeException}，因此属于非受检异常：业务代码可以直接
 * {@code throw new PlantNotFoundException()}，不需要在每一层方法签名上写
 * {@code throws}。</p>
 *
 * <p>与普通 RuntimeException 不同，本异常强制携带 {@link ErrorCode}，
 * 因此全局异常处理器能够统一知道应该返回什么 HTTP Status、业务 code 和 message。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 该业务异常对应的标准错误定义。 */
    private final ErrorCode errorCode;

    /**
     * 使用 ErrorCode 自带的默认 message 创建异常。
     *
     * @param errorCode 标准错误定义
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    /**
     * 使用自定义 message 创建异常。
     *
     * <p>适合“错误类型相同，但是希望返回更具体提示”的场景。
     * 例如搜索词过长仍属于 INVALID_SEARCH_QUERY，但 message 可以说明最大长度。</p>
     *
     * @param errorCode 标准错误定义
     * @param message 本次异常的具体提示
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
