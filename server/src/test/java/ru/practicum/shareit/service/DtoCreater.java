package ru.practicum.shareit.service;

import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;

public class DtoCreater {
    protected static final Long NEGATIVE_ID = -1L;
    protected static final Long INVALID_ID = 999999L;

    protected static UserDto makeUserDto(String email, String name) {
        return UserDto.builder().name(name).email(email).build();
    }

    protected static UserDto makeUserDtoWithId(Long id, String email, String name) {
        return UserDto.builder().id(id).name(name).email(email).build();
    }

    protected static ItemDto makeItemDto(String name, String description, boolean available, Long requestId) {
        return ItemDto.builder().name(name).description(description).available(available).requestId(requestId).build();
    }

    protected static CommentDto makeCommentDto(String text, String authorName, LocalDateTime created) {
        return CommentDto.builder().text(text).authorName(authorName).created(created).build();
    }

    protected static ItemDto makeUserDto(Long id, String name, String description, boolean available, Long requestId) {
        return ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .requestId(requestId)
                .build();
    }

    protected static ItemRequestDto makeItemRequestDto(String description,
                                              Long userId,
                                              LocalDateTime creationDate) {
        return ItemRequestDto.builder().description(description).userId(userId).creationDate(creationDate).build();
    }

    protected static BookingDto makeBookingDto(LocalDateTime startDate,
                                      LocalDateTime endDate,
                                      Long itemId,
                                      Long userId,
                                      BookingStatus status) {
        return BookingDto.builder().startDate(startDate).endDate(endDate).itemId(itemId).userId(userId).status(status).build();
    }

    protected static ItemRequestDto makeItemRequestDto(String description) {
        return ItemRequestDto.builder().description(description).build();
    }
}