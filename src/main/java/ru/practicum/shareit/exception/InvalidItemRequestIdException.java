package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidItemRequestIdException extends RuntimeException {
    public InvalidItemRequestIdException(long itemRequestId) {
        super("There is no request with id = " + itemRequestId + " for item");
    }
}