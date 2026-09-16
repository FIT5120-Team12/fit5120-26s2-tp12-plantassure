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

/** REST API 全局异常处理器。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(errorCode, exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {

        /*
         * Iteration 1 旧逻辑：
         * ErrorCode errorCode = "q".equals(exception.getParameterName())
         *         ? ErrorCode.INVALID_SEARCH_QUERY
         *         : ErrorCode.INVALID_REQUEST;
         *
         * Iteration 2 增加 compare 的必填 plantIds，因此给它稳定的专用错误码。
         */
        ErrorCode errorCode = switch (exception.getParameterName()) {
            case "q" -> ErrorCode.INVALID_SEARCH_QUERY;
            case "plantIds" -> ErrorCode.INVALID_COMPARE_SELECTION;
            default -> ErrorCode.INVALID_REQUEST;
        };

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(errorCode, errorCode.getDefaultMessage(), request.getRequestURI()));
    }

    /**
     * 处理 enum/数字等 HTTP 参数类型错误。
     * Catalog 枚举值不合法时返回 INVALID_FILTER；其他转换问题保持 INVALID_REQUEST。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        String parameter = exception.getName();
        ErrorCode errorCode = ("environmentalConcern".equals(parameter)
                || "originStatus".equals(parameter))
                ? ErrorCode.INVALID_FILTER
                : ErrorCode.INVALID_REQUEST;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(errorCode, errorCode.getDefaultMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(errorCode, errorCode.getDefaultMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        log.error("Unexpected backend error. path={}", request.getRequestURI(), exception);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(buildResponse(errorCode, errorCode.getDefaultMessage(), request.getRequestURI()));
    }

    private ErrorResponse buildResponse(ErrorCode errorCode, String message, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .path(path)
                .build();
    }
}
