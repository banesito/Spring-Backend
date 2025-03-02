package com.backend.gjejpune.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.backend.gjejpune.demo.payload.response.ErrorResponse;
import com.backend.gjejpune.demo.payload.response.MessageResponse;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle HTTP method not supported exception
     * Returns a custom error message with supported methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String path = request.getDescription(false).substring(4); // Remove "uri=" prefix
        String supportedMethods = Arrays.stream(ex.getSupportedMethods())
                .collect(Collectors.joining(", "));
        
        String errorMessage = String.format(
                "Method %s is not supported for this request. Supported methods are: %s",
                ex.getMethod(),
                supportedMethods
        );
        
        logger.warn("Method not allowed: {} for path: {}", ex.getMethod(), path);
        
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(
                    HttpStatus.METHOD_NOT_ALLOWED.value(),
                    errorMessage,
                    path
                ));
    }
    
    /**
     * Handle validation errors for request bodies
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String path = request.getDescription(false).substring(4);
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation error for path: {}", path);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation failed",
                    path,
                    errors
                ));
    }
    
    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParams(MissingServletRequestParameterException ex, WebRequest request) {
        String path = request.getDescription(false).substring(4);
        String errorMessage = String.format("Required parameter '%s' of type '%s' is missing", 
                ex.getParameterName(), ex.getParameterType());
        
        logger.warn("Missing parameter: {} for path: {}", ex.getParameterName(), path);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    errorMessage,
                    path
                ));
    }
    
    /**
     * Handle type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String path = request.getDescription(false).substring(4);
        String errorMessage = String.format("Parameter '%s' should be of type '%s'", 
                ex.getName(), ex.getRequiredType().getSimpleName());
        
        logger.warn("Type mismatch: {} for path: {}", ex.getName(), path);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    errorMessage,
                    path
                ));
    }
    
    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String path = request.getDescription(false).substring(4);
        Map<String, String> errors = new HashMap<>();
        
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Constraint violation for path: {}", path);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation failed",
                    path,
                    errors
                ));
    }
    
    /**
     * Handle malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMalformedJson(HttpMessageNotReadableException ex, WebRequest request) {
        String path = request.getDescription(false).substring(4);
        String errorMessage = "Malformed JSON request";
        
        logger.warn("Malformed JSON for path: {}", path);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    errorMessage,
                    path
                ));
    }
    
    /**
     * Handle 404 errors
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException ex, WebRequest request) {
        String path = request.getDescription(false).substring(4);
        String errorMessage = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
        
        logger.warn("No handler found: {} {} for path: {}", ex.getHttpMethod(), ex.getRequestURL(), path);
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    errorMessage,
                    path
                ));
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        String path = request.getDescription(false).substring(4);
        String errorMessage = "An unexpected error occurred";
        
        logger.error("Unexpected error for path: {}", path, ex);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    errorMessage,
                    path
                ));
    }
} 