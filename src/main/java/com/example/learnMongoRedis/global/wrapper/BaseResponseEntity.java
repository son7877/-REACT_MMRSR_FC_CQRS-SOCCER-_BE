package com.example.learnMongoRedis.global.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@RequiredArgsConstructor
public class BaseResponseEntity<T> {
    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    public BaseResponseEntity(T data, String message, HttpStatus status) {
        this.data = data;
        this.message = message;
        this.code = status.value();
        this.status = status;
    }

    public BaseResponseEntity(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
        this.code = status.value();
    }

    public BaseResponseEntity(String message) {
        this.message = message;
    }

    public static<T> ResponseEntity<BaseResponseEntity<T>> ok(T data, String message) {
        BaseResponseEntity<T> baseResponseEntity = new BaseResponseEntity<>(data, message, HttpStatus.OK);
        return ResponseEntity.status(baseResponseEntity.getStatus()).body(baseResponseEntity);
    }

    public static ResponseEntity<BaseResponseEntity<?>> ok(String message) {
        BaseResponseEntity<?> baseResponseEntity = new BaseResponseEntity<>(message, HttpStatus.OK);
        return ResponseEntity.status(baseResponseEntity.getStatus()).body(baseResponseEntity);
    }

    public static<T> ResponseEntity<BaseResponseEntity<T>> create(T data, String message) {
        BaseResponseEntity<T> baseResponseEntity = new BaseResponseEntity<>(data, message, HttpStatus.CREATED);
        return ResponseEntity.status(baseResponseEntity.getStatus()).body(baseResponseEntity);
    }

    public static ResponseEntity<BaseResponseEntity<?>> create(String message) {
        BaseResponseEntity<?> baseResponseEntity = new BaseResponseEntity<>(message, HttpStatus.CREATED);
        return ResponseEntity.status(baseResponseEntity.getStatus()).body(baseResponseEntity);
    }

    public static <T> ResponseEntity<BaseResponseEntity<?>> fail(HttpStatus httpStatus, String message) {
        BaseResponseEntity<T> baseResponseEntity = new BaseResponseEntity<>(null, message, httpStatus);
        return ResponseEntity.status(baseResponseEntity.getStatus()).body(baseResponseEntity);
    }
}
