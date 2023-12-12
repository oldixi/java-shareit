package ru.practicum.shareit_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidItemAttrsException extends RuntimeException {
    public InvalidItemAttrsException() {
        super("Item validation has been failed. Invalid attributes");
    }
}