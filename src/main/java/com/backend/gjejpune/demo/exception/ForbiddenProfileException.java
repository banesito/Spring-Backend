package com.backend.gjejpune.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenProfileException extends RuntimeException {
    
    public ForbiddenProfileException(String message) {
        super(message);
    }
    
    public ForbiddenProfileException(String message, Throwable cause) {
        super(message, cause);
    }
} 