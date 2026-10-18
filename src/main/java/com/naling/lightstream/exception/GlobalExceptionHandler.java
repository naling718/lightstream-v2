package com.naling.lightstream.exception;

import com.naling.lightstream.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<ApiError> handleClaimNotFound(ClaimNotFoundException ex) {
        ApiError body = new ApiError(404, "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(RateLimitExceededException ex) {
        ApiError body = new ApiError(429, "Too Many Requests", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "300").body(body);
    }

    @ExceptionHandler({RedisConnectionFailureException.class, QueryTimeoutException.class})
    public ResponseEntity<ApiError> handleRedisDown(Exception ex) {
        ApiError body = new ApiError(503, "Service Unavailable", "Storage is temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public  ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex){
        ApiError body = new ApiError(400, "Bad Request", "Text must not be blank");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex) {
        ApiError body = new ApiError(400, "Bad Request", "Malformed JSON request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            ApiError body = new ApiError(status.value(), "Request Error", ex.getMessage());
            return ResponseEntity.status(status).body(body);
        }
        log.error("Unexpected error", ex);
        ApiError body = new ApiError(500, "Internal Server Error", "Unexpected error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
