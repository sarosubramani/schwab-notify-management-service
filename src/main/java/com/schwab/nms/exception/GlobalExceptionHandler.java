package com.schwab.nms.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        LOGGER.info("Enter: handleResourceNotFound");
        try {
            ApiError apiError = new ApiError(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase(),
                    ex.getMessage(),
                    request.getRequestURI());
            LOGGER.info("Exit: handleResourceNotFound");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
        } catch (Exception e) {
            LOGGER.error("Error in handleResourceNotFound", e);
            throw e;
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        LOGGER.info("Enter: handleValidationException");
        try {
            Map<String, String> validationErrors = new HashMap<>();
            for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
                validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }

            ApiError apiError = new ApiError(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    validationErrors.toString(),
                    request.getRequestURI());
            LOGGER.info("Exit: handleValidationException");
            return ResponseEntity.badRequest().body(apiError);
        } catch (Exception e) {
            LOGGER.error("Error in handleValidationException", e);
            throw e;
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        LOGGER.info("Enter: handleIllegalArgumentException");
        try {
            ApiError apiError = new ApiError(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    ex.getMessage(),
                    request.getRequestURI());
            LOGGER.info("Exit: handleIllegalArgumentException");
            return ResponseEntity.badRequest().body(apiError);
        } catch (Exception e) {
            LOGGER.error("Error in handleIllegalArgumentException", e);
            throw e;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex, HttpServletRequest request) {
        LOGGER.info("Enter: handleGeneralException");
        try {
            ApiError apiError = new ApiError(
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    ex.getMessage(),
                    request.getRequestURI());
            LOGGER.info("Exit: handleGeneralException");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
        } catch (Exception e) {
            LOGGER.error("Error in handleGeneralException", e);
            throw e;
        }
    }

    public record ApiError(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path) {
    }
}
