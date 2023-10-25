package ru.practicum.shareit.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    long id;
    String description;
    User requestor;
    @JsonProperty("created") LocalDateTime creationDate;
}