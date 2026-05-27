package com.enterprise.news.exception;

import com.enterprise.news.common.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBusiness(BusinessException ex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", ex.getHttpStatus().value());
        payload.put("error", ex.getHttpStatus().getReasonPhrase());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.fail(ex.getMessage(), payload));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", HttpStatus.BAD_REQUEST.value());
        payload.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        payload.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(ApiResponse.fail("请求参数校验失败", payload));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleGeneric(Exception ex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        payload.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        payload.put("detail", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("系统异常：" + ex.getMessage(), payload));
    }
}