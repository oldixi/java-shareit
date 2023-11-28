package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithRequestId;

import java.util.List;

@Service
public interface ItemService {
    ItemDtoWithRequestId createItem(long userId, ItemDto itemDto);

    Item updateItem(long userId, long itemId, ItemDto itemDto);

    void deleteItem(long userId, long itemId);

    ItemDto getItemById(long userId, long itemId);

    ItemDto getItemByIdAndUserIdNot(long userId, long itemId);

    ItemDtoWithCommentsAndBookingInfo getItemByIdWithCommentsAndBookingInfo(long userId, long itemId);

    List<ItemDtoWithBookingInfo> getItemsByUserId(long userId, Integer from, Integer size);

    List<ItemDto> searchItems(long userId, String text, Integer from, Integer size);

    CommentDto createComment(long userId, long itemId, Comment text);

    void checkItem(long itemId);
}