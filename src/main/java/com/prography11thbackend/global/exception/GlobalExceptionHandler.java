package com.prography11thbackend.global.exception;

import com.prography11thbackend.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Object> response = ApiResponse.error(
                errorCode.getCode(),
                errorCode.getMessage()
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        ApiResponse<Object> response = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
