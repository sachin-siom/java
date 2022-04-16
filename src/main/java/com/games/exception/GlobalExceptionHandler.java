package com.games.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice("com.games")
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException resourceNotFoundException) {
        return new ResponseEntity(Resource.builder().message(resourceNotFoundException.getMessage()).errorCode(resourceNotFoundException.getErrorId()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        FieldError fieldError = result.getFieldError();
        String code = fieldError.getCode();
        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage();
        message = "{ \"Code\":\"" + code + "\",\"field\":\"" + field + "\",\"Message\":\"" + message + "\"}";
        return new ResponseEntity(Resource.builder().message(message).errorCode(-1).build(), HttpStatus.BAD_REQUEST);
    }
}

@Data
@Builder
class Resource {
    private String message;
    private int errorCode;
}
