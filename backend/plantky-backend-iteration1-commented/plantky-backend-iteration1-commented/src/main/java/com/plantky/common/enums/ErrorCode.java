package com.plantky.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 后端统一业务错误码。 */
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

    INVALID_PAGE(
            HttpStatus.BAD_REQUEST,
            "INVALID_PAGE",
            "page must be zero or greater."),

    INVALID_PAGE_SIZE(
            HttpStatus.BAD_REQUEST,
            "INVALID_PAGE_SIZE",
            "size must be between 1 and 100."),

    INVALID_FILTER(
            HttpStatus.BAD_REQUEST,
            "INVALID_FILTER",
            "One or more catalog filters are invalid."),

    INVALID_SORT(
            HttpStatus.BAD_REQUEST,
            "INVALID_SORT",
            "The requested sort is not supported."),

    INVALID_COMPARE_SELECTION(
            HttpStatus.BAD_REQUEST,
            "INVALID_COMPARE_SELECTION",
            "plantIds must contain 2 to 3 unique positive integer IDs."),

    INVALID_IMAGE(
            HttpStatus.BAD_REQUEST,
            "INVALID_IMAGE",
            "Please upload a valid JPG, PNG or WebP image."),

    IDENTIFICATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "IDENTIFICATION_FAILED",
            "Plant identification could not be completed."),

    IDENTIFICATION_SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "IDENTIFICATION_SERVICE_UNAVAILABLE",
            "Plant identification is temporarily unavailable. Please try again or search for the plant by name."),

    PLANT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLANT_NOT_FOUND",
            "Plant not found."),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "Unexpected backend error.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
