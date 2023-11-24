package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .creationDate(itemRequest.getCreationDate())
                .userId(itemRequest.getUser().getId())
                .build();
    }

    public static ItemRequestDtoWithItems toItemRequestDtoWithItems(ItemRequest itemRequest, List<ItemDto> items) {
        return ItemRequestDtoWithItems.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .creationDate(itemRequest.getCreationDate())
                .userId(itemRequest.getUser().getId())
                .items(items)
                .build();
    }

    public static List<ItemRequestDto> toItemRequestDto(Page<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    public static List<ItemRequestDto> toItemRequestDto(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    public static ItemRequest toItemRequest(long itemRequestId, ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .id(itemRequestId)
                .description(itemRequestDto.getDescription())
                .creationDate(itemRequestDto.getCreationDate())
                .user(user)
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .creationDate(itemRequestDto.getCreationDate())
                .user(user)
                .build();
    }
}