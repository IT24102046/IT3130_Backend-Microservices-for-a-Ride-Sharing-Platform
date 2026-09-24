package com.ridelink.ride.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, req);
    }
    
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(
            InvalidStatusTransitionException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, req);
    }
    
    @ExceptionHandler(NoAvailableDriverException.class)
    public ResponseEntity<ErrorResponse> handleNoDriver(
            NoAvailableDriverException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, req);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN, req);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        return buildResponse("Access denied", HttpStatus.FORBIDDEN, req);
    }
    
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(
            ServiceUnavailableException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, req);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return buildResponse(message, HttpStatus.BAD_REQUEST, req);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return buildResponse("Internal server error",
            HttpStatus.INTERNAL_SERVER_ERROR, req);
    }
    
    private ResponseEntity<ErrorResponse> buildResponse(
            String message, HttpStatus status, HttpServletRequest req) {
        ErrorResponse error = new ErrorResponse(
            message,
            status.value(),
            LocalDateTime.now(),
            req.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}