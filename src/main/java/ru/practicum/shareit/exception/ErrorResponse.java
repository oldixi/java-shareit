package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    String message;

    public ErrorResponse(String message) {
        this.message = message;
    }
}