package io.github.kuonjiarisu.backend.support.api;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        log.warn(
            "Business request rejected: method={} path={} reason={}",
            request.getMethod(),
            request.getRequestURI(),
            exception.getMessage()
        );
        return build(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccess(DataAccessException exception, HttpServletRequest request) {
        log.error(
            "Data access failed: method={} path={}",
            request.getMethod(),
            request.getRequestURI(),
            exception
        );
        return build(HttpStatus.BAD_REQUEST, "数据操作失败，请检查相关数据是否仍被引用或格式是否正确");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var message = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getDefaultMessage() == null ? error.getField() + " 参数不合法" : error.getDefaultMessage())
            .findFirst()
            .orElse("请求参数不合法");
        log.warn(
            "Request validation failed: method={} path={} reason={}",
            request.getMethod(),
            request.getRequestURI(),
            message
        );
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        log.warn(
            "Authentication failed: method={} path={} reason={}",
            request.getMethod(),
            request.getRequestURI(),
            exception.getMessage()
        );
        return build(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOther(Exception exception, HttpServletRequest request) {
        log.error(
            "Unhandled request failure: method={} path={}",
            request.getMethod(),
            request.getRequestURI(),
            exception
        );
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private ResponseEntity<Object> build(HttpStatus status, String message) {
        var body = new LinkedHashMap<String, Object>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
