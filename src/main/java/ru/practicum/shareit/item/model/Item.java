package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Item {
    private long id;
    @NotNull private User owner;
    private String name;
    private String description;
    private boolean available;
    private ItemRequest request;
}