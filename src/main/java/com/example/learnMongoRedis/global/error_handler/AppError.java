package com.example.learnMongoRedis.global.error_handler;

import com.example.learnMongoRedis.global.resouce.AppString;
import org.springframework.http.HttpStatus;

public sealed abstract class AppError extends RuntimeException permits AppError.Expected, AppError.Unexpected {

    private AppError() {
    }

    public abstract String getMessage();

    public abstract HttpStatus getHttpStatus();

    // 허용된 예외
    public static abstract sealed class Expected extends AppError permits
            Expected.AccessDeniedException,
            Expected.BusinessRuleException,
            Expected.EntityNotFoundException,
            Expected.UnauthorizedException,
            Expected.ValidationFailedException
    {

        private Expected() {
        }

        public static final class ValidationFailedException extends Expected {
            private final String message;

            public ValidationFailedException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.BAD_REQUEST;
            }
        }

        public static final class EntityNotFoundException extends Expected {
            private final String message;

            public EntityNotFoundException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.NOT_FOUND;
            }
        }

        public static final class AccessDeniedException extends Expected {
            private final String message;

            public AccessDeniedException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.FORBIDDEN;
            }
        }

        public static final class BusinessRuleException extends Expected {
            private final String message;

            public BusinessRuleException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.CONFLICT;
            }
        }

        public static final class UnauthorizedException extends Expected {
            private final String message;

            public UnauthorizedException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.UNAUTHORIZED;
            }
        }
    }

    // 비정상 예외
    public static abstract sealed class Unexpected extends AppError permits
            Unexpected.DataAccessException,
            Unexpected.EntityNotFoundException,
            Unexpected.ExternalServiceException,
            Unexpected.IllegalArgumentException,
            Unexpected.NullPointerException,
            Unexpected.SystemException,
            Unexpected.UnauthorizedException
    {

        private Unexpected() {
        }

        public static final class DataAccessException extends Unexpected {
            private final String message;

            public DataAccessException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }
        }

        public static final class UnauthorizedException extends Unexpected {
            private final String message;

            public UnauthorizedException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.UNAUTHORIZED;
            }
        }

        public static final class EntityNotFoundException extends Unexpected {
            private final String message;

            public EntityNotFoundException(String message) {this.message = message;}
            @Override
            public String getMessage() {
                return message;
            }
            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.NOT_FOUND;
            }
        }


        public static final class NullPointerException extends Unexpected {
            private final String message;

            public NullPointerException(String message) {
                this.message = message;
            }
            @Override
            public String getMessage() {
                return message;
            }
            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }

        public static final class IllegalArgumentException extends Unexpected {
            private final String message;

            public IllegalArgumentException(String message) {
                this.message = message;
            }
            @Override
            public String getMessage() {
                return message;
            }
            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.BAD_REQUEST;
            }
        }

        public static final class SystemException extends Unexpected {
            private final String message;

            public SystemException(String message) {
                this.message = message;
            }
            @Override
            public String getMessage() {
                return message;
            }
            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }

        public static final class ExternalServiceException extends Unexpected {
            private final String message;

            public ExternalServiceException(String message) {
                this.message = message;
            }
            @Override
            public String getMessage() {
                return message;
            }
            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.BAD_GATEWAY;
            }
        }
    }
}