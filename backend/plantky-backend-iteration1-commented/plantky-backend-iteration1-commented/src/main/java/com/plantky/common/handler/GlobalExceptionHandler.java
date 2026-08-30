package com.plantky.common.handler;

import com.plantky.common.enums.ErrorCode;
import com.plantky.common.exception.BusinessException;
import com.plantky.common.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * REST API 全局异常处理器。
 *
 * <p>{@code @RestControllerAdvice} 可以拦截所有 Controller 在请求处理过程中抛出的异常，
 * 将异常统一转换为 HTTP Response。这样 Controller/Service 不需要到处写 try-catch。</p>
 *
 * <p>设计原则：</p>
 * <ol>
 *     <li>业务异常返回可理解、稳定的业务错误码；</li>
 *     <li>参数错误统一返回 400；</li>
 *     <li>未知异常只在服务端日志记录完整堆栈，不能把内部实现细节暴露给前端。</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有 BusinessException 及其子类。
     *
     * <p>例如 PlantNotFoundException、InvalidSearchQueryException 都会进入这里。</p>
     *
     * @param exception 业务异常
     * @param request 当前 HTTP 请求，用于读取请求路径
     * @return 带正确 HTTP Status 的统一 ErrorResponse
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        ErrorResponse response = buildResponse(
                errorCode,
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 处理缺少 query parameter 的情况。
     *
     * <p>例如调用：</p>
     * <pre>GET /api/v1/plants/search</pre>
     * <p>但接口要求必须存在 {@code q}，Spring MVC 会在进入 Controller 方法之前直接抛出
     * MissingServletRequestParameterException。</p>
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {

        // q 是当前项目明确的搜索参数，因此缺失 q 时返回更具体的 INVALID_SEARCH_QUERY。
        // 如果未来增加其他必填参数，则统一归类为 INVALID_REQUEST。
        ErrorCode errorCode = "q".equals(exception.getParameterName())
                ? ErrorCode.INVALID_SEARCH_QUERY
                : ErrorCode.INVALID_REQUEST;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(
                        errorCode,
                        errorCode.getDefaultMessage(),
                        request.getRequestURI()));
    }

    /**
     * 处理 Bean Validation / 参数类型转换异常。
     *
     * <p>典型场景：</p>
     * <ul>
     *     <li>{@code @Positive @PathVariable Long plantId} 收到 0 或负数；</li>
     *     <li>plantId 本应是 Long，但 URL 中传入 abc。</li>
     * </ul>
     */
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            Exception exception,
            HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(
                        errorCode,
                        errorCode.getDefaultMessage(),
                        request.getRequestURI()));
    }

    /**
     * 最后一层兜底异常处理。
     *
     * <p>这里捕获未被前面处理器覆盖的异常，例如数据库连接异常、程序 bug 等。
     * 完整异常堆栈写入服务器日志，方便开发人员定位；但是前端只得到通用 500 响应，
     * 避免泄漏 SQL、服务器路径、类名或其他内部信息。</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        log.error("Unexpected backend error. path={}", request.getRequestURI(), exception);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(
                        errorCode,
                        errorCode.getDefaultMessage(),
                        request.getRequestURI()));
    }

    /**
     * 统一创建 ErrorResponse，避免多个异常处理方法重复 builder 代码。
     *
     * @param errorCode 错误定义
     * @param message 本次响应实际返回的错误信息
     * @param path 请求路径
     * @return ErrorResponse 对象
     */
    private ErrorResponse buildResponse(ErrorCode errorCode, String message, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .path(path)
                .build();
    }
}
