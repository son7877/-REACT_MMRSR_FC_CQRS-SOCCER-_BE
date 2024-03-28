//package com.example.learnMongoRedis.global.handle_error;
//
//import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
//import lombok.Getter;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//public sealed class Result<T> permits Result.Success, Result.Failure {
//
//    private Result() {
//        // Private constructor to prevent direct instantiation
//    }
//
//    public boolean isSuccess() {
//        return this instanceof Success;
//    }
//
//    public boolean isFailure() {
//        return this instanceof Failure;
//    }
//
//    @Getter
//    public static final class Success<T> extends Result<T> {
//        private final T data;
//
//        public Success(T data) {
//            this.data = data;
//        }
//
//    }
//
//    @Getter
//    public static final class Failure<T> extends Result<T> {
//        private final AppError error;
//
//        public Failure(AppError error) {
//            this.error = error;
//        }
//
//    }
//
//    public static <T> Result<T> success(T data) {
//        return new Success<>(data);
//    }
//
//    public static <T> Result<T> failure(AppError error) {
//        return new Failure<>(error);
//    }
//
//
//    public <U> Result<U> map(java.util.function.Function<? super T, ? extends U> mapper) {
//        if (this instanceof Success<T> success) {
//            return new Success<>(mapper.apply(success.getData()));
//        } else if (this instanceof Failure<T> failure) {
//            // No unchecked cast needed; but assumes you can access the same error from failure.
//            return new Failure<>(failure.getError());
//        } else {
//            // This should never happen if you have all branches of your sealed class covered
//            throw new AssertionError("Unknown Result subtype: " + this);
//        }
//    }
//
//
//}