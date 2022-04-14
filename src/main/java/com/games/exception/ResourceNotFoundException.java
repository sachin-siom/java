package com.games.exception;

import lombok.Data;

@Data
public class ResourceNotFoundException extends RuntimeException {
    private String message;
    private Integer errorId;
    private String trace;

    public ResourceNotFoundException(String message, int errorId) {
        super(message);
        this.message = message;
        this.errorId = errorId;
    }
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}


