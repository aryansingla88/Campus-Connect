package com.campus.Campus_Connect.common.exception;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.post.exception.PostNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // TODO: Temporary. Replace usages with ResourceNotFoundException and remove.
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePostNotFoundException(
            PostNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ex.getMessage()));
    }



// ------------------------------------------------------------
// 400 Bad Request
// ------------------------------------------------------------
        @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)                         //spring
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {

        FieldError fieldError = ex.getBindingResult().getFieldError();

        String message = "Validation failed.";

        if (fieldError != null) {
            message = fieldError.getDefaultMessage();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.failure(message)
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)                                 //spring
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.failure("Invalid request data.")
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)                                    //  Spring
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {

        String message = "Validation failed.";

        ConstraintViolation<?> violation = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .orElse(null);

        if (violation != null) {
            message = violation.getMessage();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.failure(message)
                );
    }

// ------------------------------------------------------------
// 401 Unauthorized
// ------------------------------------------------------------
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

// ------------------------------------------------------------
// 403 Forbidden
// ------------------------------------------------------------

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            ForbiddenException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        ApiResponse.failure("Access denied.")
                );
    }

// ------------------------------------------------------------
// 404 Not Found
// ------------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }



// ------------------------------------------------------------
// 405 Method Not Allowed
// ------------------------------------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(
                        ApiResponse.failure("HTTP method not supported.")
                );
    }

// ------------------------------------------------------------
// 409 Conflict
// ------------------------------------------------------------

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            ConflictException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponse.failure("Resource already exists.")
                );
    }

// ------------------------------------------------------------
// 413 Payload Too Large
// ------------------------------------------------------------

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<ApiResponse<Void>> handlePayloadTooLarge(
            PayloadTooLargeException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(
                        ApiResponse.failure("Uploaded file is too large.")
                );
    }

// ------------------------------------------------------------
// 415 Unsupported Media Type
// ------------------------------------------------------------

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(
            UnsupportedMediaTypeException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(
                        ApiResponse.failure("Unsupported media type.")
                );
    }
// ------------------------------------------------------------
// 429 Too Many Requests
// ------------------------------------------------------------

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequests(
            TooManyRequestsException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

// ------------------------------------------------------------
// 500 Internal Server Error
// ------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex
    ) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }

// ------------------------------------------------------------
// 503 Service Unavailable
// ------------------------------------------------------------

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceUnavailable(
            ServiceUnavailableException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponse.failure(ex.getMessage())
                );
    }
}