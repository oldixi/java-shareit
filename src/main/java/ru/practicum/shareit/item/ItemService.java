package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookingInfo;

import java.util.List;

@Service
public interface ItemService {
    Item createItem(long userId, ItemDto itemDto);

    Item updateItem(long userId, long itemId, ItemDto itemDto);

    void deleteItem(long userId, long itemId);

    ItemDto getItemById(long userId, long itemId);

    ItemDto getItemByIdAndUserIdNot(long userId, long itemId);

    ItemDtoWithCommentsAndBookingInfo getItemByIdWithCommentsAndBookingInfo(long userId, long itemId);

    ItemDtoWithCommentsAndBookingInfo getItemByIdAndUserId(long itemId, long userId);

    List<ItemDtoWithBookingInfo> getItemsByUserId(long userId);

    List<ItemDto> searchItems(long userId, String text);

    CommentDto createComment(long userId, long itemId, Comment text);

    void checkItem(long userId, long itemId);

    void checkItem(long itemId);
}