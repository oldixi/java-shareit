package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item createItem(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Request for item {} of user {} creation", itemDto.getName(), userId);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Request for item {} of user {} update", itemId, userId);
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        log.info("Request for item {} of user {} deletion", itemId, userId);
        itemService.deleteItem(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        log.info("Request for get item {} of user {}", itemId, userId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Request for get items of user {}", userId);
        return itemService.getItemsByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader("X-Sharer-User-Id") long userId, @RequestParam String text) {
        log.info("Request for get searching items like {}", text);
        return itemService.searchItems(userId, text);
    }
}