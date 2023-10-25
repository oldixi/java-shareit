package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidUserIdException extends RuntimeException {
    public InvalidUserIdException(long userId) {
        super("There is no user with id = " + userId);
    }
}