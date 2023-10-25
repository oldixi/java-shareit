package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InvalidItemArrtsException;
import ru.practicum.shareit.exception.InvalidItemIdException;
import ru.practicum.shareit.exception.InvalidUserIdException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public Item createItem(long userId, ItemDto itemDto) {
        checkUser(userId);
        if (isItemAttrsEmpty(itemDto)) {
            throw new InvalidItemArrtsException();
        }
        return itemRepository.createItem(userId, itemDto);
    }

    @Override
    public Item updateItem(long userId, long itemId, ItemDto itemDto) {
        checkUser(userId);
        if (!isItemValid(userId, itemId)) {
            throw new InvalidItemIdException(itemId);
        }
        return itemRepository.updateItem(userId, itemId, itemDto);
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        checkUser(userId);
        itemRepository.deleteItem(userId, itemId);
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        checkUser(userId);
        return itemRepository.getItemById(itemId).orElseThrow(() -> new InvalidItemIdException(itemId));
    }

    @Override
    public List<ItemDto> getItemsByUserId(long userId) {
        checkUser(userId);
        return itemRepository.getItemsByUserId(userId);
    }

    @Override
    public List<ItemDto> searchItems(long userId, String text) {
        if (text == null || text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        checkUser(userId);
        return itemRepository.searchItems(userId, text);
    }

    private boolean isItemAttrsEmpty(ItemDto itemDto) {
        return itemDto.getDescription() == null || itemDto.getDescription().isBlank() || itemDto.getDescription().isEmpty() ||
                itemDto.getName() == null || itemDto.getName().isBlank() || itemDto.getName().isEmpty() ||
                itemDto.getAvailable() == null;
    }

    private void checkUser(long userId) {
        if (!userService.isUserValid(userId)) {
            throw new InvalidUserIdException(userId);
        }
    }

    private boolean isInvalidId(long id) {
        return id <= 0;
    }

    private boolean isItemExists(long userId, long id) {
        return itemRepository.isItemsExists(userId) && itemRepository.isItemExists(userId, id);
    }

    public boolean isItemValid(long userId, long id) {
        return !isInvalidId(id) && isItemExists(userId, id);
    }
}