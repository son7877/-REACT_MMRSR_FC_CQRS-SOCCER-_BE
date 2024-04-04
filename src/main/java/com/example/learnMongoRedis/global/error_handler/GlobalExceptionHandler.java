package com.example.learnMongoRedis.global.error_handler;

import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.LocalDateTime;
import java.util.Arrays;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppError.class)
    public ResponseEntity<BaseResponseEntity<?>> handleMethodArgumentNotValidException(AppError error, ServletWebRequest request) {
        logErrorIfUnexpected(error, request);
        return BaseResponseEntity.fail(error.getHttpStatus(),error.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponseEntity<?>> handleAllUnexpectedExceptions(Exception ex, ServletWebRequest request) {
        ex.printStackTrace();
        log.error(toLogEntry(ex, request));
        return BaseResponseEntity.fail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // 나중에 테스트 환경에서는 동작하지 않도록 변경
//    @Profile("prod")
    private void logErrorIfUnexpected(AppError ex, ServletWebRequest request) {
        if (ex instanceof AppError.Unexpected) {
            log.error(toLogEntry(ex, request));
        }
    }

    private String toLogEntry(Exception e, ServletWebRequest request) {
        e.printStackTrace();
        String url = request.getDescription(false);
        String httpMethod = request.getHttpMethod().toString();
        return String.format("""
                Time: %s,
                Uid: %s,
                Method: %s,
                URL: %s,
                Status: %s,
                Message: %s
                """,
                LocalDateTime.now(),
                SecurityContextHolder.getContext().getAuthentication().getName(),
                httpMethod,
                url,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage());
    }
}