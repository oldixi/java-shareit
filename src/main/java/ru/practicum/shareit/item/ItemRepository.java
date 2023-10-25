package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item createItem(long userId, ItemDto itemDto);

    Item updateItem(long userId, long itemId, ItemDto itemDto);

    void deleteItem(long userId, long itemId);

    Optional<ItemDto> getItemById(long itemId);

    List<ItemDto> getItemsByUserId(long userId);

    List<ItemDto> searchItems(long userId, String text);

    boolean isItemExists(long userId, long itemId);

    boolean isItemsExists(long userId);
}