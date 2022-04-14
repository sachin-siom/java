package com.games.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity handleResourceNotFoundException(ResourceNotFoundException resourceNotFoundException) {
        return new ResponseEntity(Resource.builder().message(resourceNotFoundException.getMessage()).errorCode(resourceNotFoundException.getErrorId()).build(), HttpStatus.BAD_REQUEST);
    }
}

@Data
@Builder
class Resource {
    private String message;
    private int errorCode;
}
