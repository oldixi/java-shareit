package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("itemMemoryRepository")
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Map<Long, Item>> usersItems = new HashMap<>();
    private long generatedId = 0;

    private long generateId() {
        return ++generatedId;
    }

    @Override
    public Item createItem(long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(generateId(), itemDto);
        usersItems.merge(userId, new HashMap<>(Map.of(item.getId(), item)), (itemsOld, itemsNew) -> {
            itemsOld.put(item.getId(), item);
            return itemsOld;
        });
        return usersItems.get(userId).get(item.getId());
    }

    @Override
    public Item updateItem(long userId, long itemId, ItemDto itemDto) {
        itemDto.setName(itemDto.getName() != null ?
                itemDto.getName() :
                usersItems.get(userId).get(itemId).getName());
        itemDto.setDescription(itemDto.getDescription() != null ?
                itemDto.getDescription() :
                usersItems.get(userId).get(itemId).getDescription());
        itemDto.setAvailable(itemDto.getAvailable() != null ?
                itemDto.getAvailable() :
                usersItems.get(userId).get(itemId).isAvailable());
        Item item = ItemMapper.toItem(itemId, itemDto);
        usersItems.computeIfPresent(userId, (id, items) -> {
            items.replace(itemId, item);
            return items;
        });
        return usersItems.get(userId).get(itemId);
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        usersItems.computeIfPresent(userId, (id, items) -> {
            items.remove(itemId);
            return items;
        });
    }

    @Override
    public Optional<ItemDto> getItemById(long itemId) {
        return usersItems.values()
                .stream()
                .flatMap(itemMap -> itemMap.values().stream())
                .filter(item-> item.getId() == itemId)
                .map(ItemMapper::toItemDto)
                .findFirst();
    }

    @Override
    public List<ItemDto> getItemsByUserId(long userId) {
        return usersItems.get(userId).values()
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(long userId, String text) {
        return usersItems.values()
                .stream()
                .flatMap(itemMap -> itemMap.values().stream())
                .filter(Item::isAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isItemExists(long userId, long itemId) {
        return usersItems.get(userId).values()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(item -> item.getId() == itemId);
    }

    @Override
    public boolean isItemsExists(long userId) {
        return usersItems.containsKey(userId);
    }
}