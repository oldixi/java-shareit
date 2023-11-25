package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithRequestId;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

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

    public static List<ItemDto> toItemDto(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public static ItemDtoWithBookingInfo toItemDtoWithBookingInfo(Item item,
                                                                  BookingDto lastBooking,
                                                                  BookingDto nextBooking) {
        return ItemDtoWithBookingInfo.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();
    }

    public static ItemDtoWithCommentsAndBookingInfo toItemDtoWithCommentsAndBookingInfo(Item item,
                                                                                        List<CommentDto> comments,
                                                                                        BookingDto lastBooking,
                                                                                        BookingDto nextBooking) {
        return ItemDtoWithCommentsAndBookingInfo.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .comments(comments)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();
    }

    public static Item toItem(long itemId, ItemDto itemDto, User user) {
        return Item.builder()
                .id(itemId)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .user(user)
                .build();
    }

    public static Item toItem(long itemId, ItemDto itemDto, User user, ItemRequest request) {
        return Item.builder()
                .id(itemId)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .user(user)
                .request(request)
                .build();
    }

    public static Item toItem(ItemDto itemDto, User user) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .user(user)
                .build();
    }

    public static Item toItem(ItemDto itemDto, User user, ItemRequest request) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .user(user)
                .request(request)
                .build();
    }

    public static Item toItem(Item item, ItemDto itemDto, User user, ItemRequest request) {
        itemDto.setName(itemDto.getName() != null ?
                itemDto.getName() :
                item.getName());
        itemDto.setDescription(itemDto.getDescription() != null ?
                itemDto.getDescription() :
                item.getDescription());
        itemDto.setAvailable(itemDto.getAvailable() != null ?
                itemDto.getAvailable() :
                item.isAvailable());
        return Item.builder()
                .id(item.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .user(user)
                .request(request)
                .build();
    }

    public static ItemDtoWithRequestId toItemDtoWithRequestId(Item item) {
        return ItemDtoWithRequestId.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .user(item.getUser())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }
}