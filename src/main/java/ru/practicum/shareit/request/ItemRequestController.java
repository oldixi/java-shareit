package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequest createItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Request for request for item {} from user {} creation", itemRequestDto.getDescription(), userId);
        return requestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoWithItems getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @PathVariable long requestId) {
        log.info("Request for get item's request {} from user {}", requestId, userId);
        return requestService.getItemRequestById(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestDtoWithItems> getItemRequestsByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Request for get  user's {} requests for items", userId);
        return requestService.getItemRequestsByRequestorId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoWithItems> getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestParam(required = false) Integer from,
                                                @RequestParam(required = false) Integer size) {
        log.info("Request for get {} requests for items from {} request", size, from);
        return requestService.getItemRequests(userId, from, size);
    }
}