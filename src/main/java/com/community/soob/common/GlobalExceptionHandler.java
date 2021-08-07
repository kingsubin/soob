package com.community.soob.common;

import com.community.soob.response.ErrorResponse;
import com.community.soob.response.ResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResultResponse<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException: " + e.getMessage());
        final ErrorResponse errorResponse = ErrorResponse.of("MethodArgumentNotValidException", e.getMessage());
        return ResultResponse.of(ResultResponse.ERROR, errorResponse);
    }

    @ExceptionHandler(InvalidValueException.class)
    protected ResultResponse<ErrorResponse> handleInvalidValueException(final InvalidValueException e) {
        log.error("handleInvalidValueException: " + e.getMessage());
        final ErrorResponse errorResponse = ErrorResponse.of("InvalidValueException", e.getMessage());
        return ResultResponse.of(ResultResponse.FAIL, errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResultResponse<ErrorResponse> handleEntityNotFoundException(final EntityNotFoundException e) {
        log.error("handleEntityNotFoundException: " + e.getMessage());
        final ErrorResponse errorResponse = ErrorResponse.of("EntityNotFoundException", e.getMessage());
        return ResultResponse.of(ResultResponse.ERROR, errorResponse);
    }

    @ExceptionHandler(BusinessException.class)
    protected ResultResponse<ErrorResponse> handleBusinessException(final BusinessException e) {
        log.error("handleBusinessException: " + e.getMessage());
        final ErrorResponse errorResponse = ErrorResponse.of("BusinessException", e.getMessage());
        return ResultResponse.of(ResultResponse.ERROR, errorResponse);
    }

    @ExceptionHandler(Exception.class)
    protected ResultResponse<ErrorResponse> handleException(final Exception e) {
        log.error("handleException: " + e.getClass() + ", " + e.getMessage());
        final ErrorResponse errorResponse = ErrorResponse.of("Exception", e.getMessage());
        return ResultResponse.of(ResultResponse.ERROR, errorResponse);
    }
}
