package ru.practicum.shareit.item;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(long itemId, ItemDto itemDto) {
        return Item.builder()
                .id(itemId)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static Item toItem(long itemId, ItemDto itemdto, User owner, ItemRequest request) {
        return Item.builder()
                .id(itemId)
                .name(itemdto.getName())
                .description(itemdto.getDescription())
                .available(itemdto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}