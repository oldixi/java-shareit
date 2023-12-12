package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.List;

@Service
public interface ItemRequestService {
    ItemRequest createItemRequest(long userId, ItemRequestDto itemRequestDto);

    ItemRequestDtoWithItems getItemRequestById(long userId, long itemRequestId);

    List<ItemRequestDtoWithItems> getItemRequestsByRequestorId(long userId);

    List<ItemRequestDtoWithItems> getItemRequests(long userId, Integer from, Integer size);
}