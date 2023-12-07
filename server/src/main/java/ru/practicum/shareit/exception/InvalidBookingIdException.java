package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidBookingIdException extends RuntimeException {
    public InvalidBookingIdException(long bookingId) {
        super("There is no booking request with id = " + bookingId);
    }
}