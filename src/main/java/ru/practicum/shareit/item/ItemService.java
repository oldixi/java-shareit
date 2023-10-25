package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
public interface ItemService {
    Item createItem(long userId, ItemDto itemDto);

    Item updateItem(long userId, long itemId, ItemDto itemDto);

    void deleteItem(long userId, long itemId);

    ItemDto getItemById(long userId, long itemId);

    List<ItemDto> getItemsByUserId(long userId);

    List<ItemDto> searchItems(long userId, String text);

    boolean isItemValid(long userId, long id);
}