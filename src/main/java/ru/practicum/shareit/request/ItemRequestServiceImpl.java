package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.InvalidItemRequestIdException;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequest createItemRequest(long userId, ItemRequestDto itemRequestDto) {
        User user = userService.getUserById(userId);
        itemRequestDto.setUserId(userId);
        itemRequestDto.setCreationDate(LocalDateTime.now());
        return itemRequestRepository.save(ItemRequestMapper.toItemRequest(itemRequestDto, user));
    }

    @Override
    public ItemRequestDtoWithItems getItemRequestById(long userId, long itemRequestId) {
        userService.checkUser(userId);
        ItemRequest itemRequest = itemRequestRepository
                .findById(itemRequestId).orElseThrow(() -> new InvalidItemRequestIdException(itemRequestId));
        return ItemRequestMapper.toItemRequestDtoWithItems(itemRequest,
                ItemMapper.toItemDto(itemRepository
                        .findByRequestUserIdOrderByRequestCreationDateDesc(itemRequest.getUser().getId())));
    }

    @Override
    public List<ItemRequestDtoWithItems> getItemRequestsByRequestorId(long userId) {
        userService.checkUser(userId);
        return itemRequestRepository.findByUserIdOrderByCreationDateDesc(userId)
                .stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDtoWithItems(itemRequest,
                        ItemMapper.toItemDto(itemRepository
                                .findByRequestUserIdOrderByRequestCreationDateDesc(itemRequest.getUser().getId()))))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDtoWithItems> getItemRequests(long userId, Integer from, Integer size) {
        userService.checkUser(userId);
        if (from == null || size == null) {
            return itemRequestRepository.findByUserIdNotOrderByCreationDateDesc(userId)
                    .stream()
                    .map(itemRequest -> ItemRequestMapper.toItemRequestDtoWithItems(itemRequest,
                            ItemMapper.toItemDto(itemRepository
                                    .findByRequestUserIdOrderByRequestCreationDateDesc(itemRequest.getUser().getId()))))
                    .collect(Collectors.toList());
        } else if (from < 0 || size <= 0) {
            throw new InvalidPathVariableException("Incorrect page parameters");
        } else {
            int pageNumber = from / size;
            final Pageable page = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
            return itemRequestRepository.findByUserIdNot(userId, page)
                    .stream()
                    .map(itemRequest -> ItemRequestMapper.toItemRequestDtoWithItems(itemRequest,
                            ItemMapper.toItemDto(itemRepository
                                    .findByRequestUserIdOrderByRequestCreationDateDesc(itemRequest.getUser().getId()))))
                    .collect(Collectors.toList());
        }
    }
}