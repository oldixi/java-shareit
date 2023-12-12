package ru.practicum.shareit_gateway.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit_gateway.item.dto.CommentDto;
import ru.practicum.shareit_gateway.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Request for item {} of user {} creation", itemDto.getName(), userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @Valid @RequestBody CommentDto text,
                                    @Positive @PathVariable long itemId) {
        log.info("Request for comment on item {} of user {} creation", itemId, userId);
        return itemClient.addComment(userId, itemId, text);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @Positive @PathVariable long itemId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Request for item {} of user {} update", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @Positive @PathVariable long itemId) {
        log.info("Request for item {} of user {} deletion", itemId, userId);
        return itemClient.deleteItem(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemByIdAndUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @Positive @PathVariable long itemId) {
        log.info("Request for get item {} of user {}", itemId, userId);
        return itemClient.getItemByIdAndUserId(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request for get {} items of user {} from {}", size, userId, from);
        return itemClient.getItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader("X-Sharer-User-Id") long userId,
                @RequestParam String text,
                @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request for get searching {} items like {} from {}", size, text, from);
        return itemClient.searchItems(userId, text, from, size);
    }
}