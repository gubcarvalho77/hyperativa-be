package com.hyperativa.be.config;

import com.hyperativa.be.exceptions.CreditCardException;
import com.hyperativa.be.exceptions.ResourceExistsException;
import com.hyperativa.be.exceptions.ResourceNotFoundException;
import com.hyperativa.be.exceptions.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);

        final var status = HttpStatus.INTERNAL_SERVER_ERROR;

        var detail = ProblemDetail.forStatus(status);
        detail.setTitle(status.getReasonPhrase());
        detail.setDetail("An unexpected error occurred. Please try again later.");
        detail.setProperty("code", "INTERNAL_ERROR");
        detail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        final var status = HttpStatus.NOT_FOUND;

        var detail = ProblemDetail.forStatus(status);
        detail.setTitle(status.getReasonPhrase());
        detail.setDetail(ex.getMessage());
        detail.setProperty("code", "RESOURCE_NOT_FOUND");
        detail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler(ResourceExistsException.class)
    protected ResponseEntity<Object> handleResourceExistsException(
            ResourceExistsException ex,
            HttpServletRequest request
    ) {
        final var status = HttpStatus.CONFLICT;

        var detail = ProblemDetail.forStatus(status);
        detail.setTitle(status.getReasonPhrase());
        detail.setDetail(ex.getMessage());
        detail.setProperty("code", "RESOURCE_FOUND");
        detail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class})
    protected ResponseEntity<Object> handleValidationException(
            ValidationException ex,
            HttpServletRequest request
    ) {
        final var status = HttpStatus.BAD_REQUEST;

        var detail = ProblemDetail.forStatus(status);
        detail.setTitle(status.getReasonPhrase());
        detail.setDetail("BUSINESS_EXCEPTION_FOUND");
        detail.setProperty("code", ex.getMessage());
        detail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler(CreditCardException.class)
    protected ResponseEntity<Object> handleCreditCardException(
            CreditCardException ex,
            HttpServletRequest request
    ) {
        final var status = HttpStatus.UNPROCESSABLE_ENTITY;

        var detail = ProblemDetail.forStatus(status);
        detail.setTitle(status.getReasonPhrase());
        detail.setDetail("UNPROCESSABLE_ENTITY_EXCEPTION_FOUND");
        detail.setProperty("code", ex.getMessage());
        detail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex) {
        var detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(detail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex) {
        var detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(detail);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        var bindingResult = ex.getBindingResult();
        var firstError = bindingResult.getAllErrors().getFirst();

        var problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail(firstError.getDefaultMessage());
        problemDetail.setProperty("code", "VALIDATION_ERROR");
        problemDetail.setProperty("field", getFieldName(firstError));
        problemDetail.setProperty("timestamp", Instant.now());

        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    private String getFieldName(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField();
        }
        return null;
    }
}
