package ru.practicum.shareit_gateway.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    String error;
    int status;
}