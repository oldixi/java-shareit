package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("ru.yandex.practicum.shareit")
public class ErrorHandler {

    @ExceptionHandler
    public ErrorResponse handleUserValidationException(final InvalidEmailException e) {
        //log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    public ErrorResponse handleInvalidPathVariableException(final InvalidPathVariableException e) {
        //log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    public ErrorResponse handleInvalidUserIdException(final InvalidUserIdException e) {
        //log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    public ErrorResponse handleInvalidItemIdException(final InvalidItemIdException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        return new ErrorResponse("Unexpected error has occurred");
    }
}