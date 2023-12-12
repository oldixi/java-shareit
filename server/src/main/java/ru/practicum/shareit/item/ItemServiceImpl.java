package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.exception.InvalidItemAttrsException;
import ru.practicum.shareit.exception.InvalidItemIdException;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.exception.PermissionDeniedException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithRequestId;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemDtoWithRequestId createItem(long userId, ItemDto itemDto) {
        if (isItemAttrsEmpty(itemDto)) {
            throw new InvalidItemAttrsException();
        }
        User user = userService.getUserById(userId);
        Item item = itemRepository.save(ItemMapper.toItem(itemDto, user,
                itemDto.getRequestId() != null ? itemRequestRepository
                        .findById(itemDto.getRequestId()).orElse(null) : null));
        log.info("Item {} is created", item.getId());
        return ItemMapper.toItemDtoWithRequestId(item);
    }

    @Override
    @Transactional
    public Item updateItem(long userId, long itemId, ItemDto itemDto) {
        if (!isItemValid(userId, itemId)) {
            throw new InvalidItemIdException(itemId);
        }
        User user = userService.getUserById(userId);
        return itemRepository.save(ItemMapper.toItem(itemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new InvalidItemIdException(itemId)), itemDto, user,
                itemDto.getRequestId() != null ? itemRequestRepository
                        .findById(itemDto.getRequestId()).orElse(null) : null));
    }

    @Override
    @Transactional
    public void deleteItem(long userId, long itemId) {
        userService.checkUser(userId);
        itemRepository.deleteByUserIdAndId(userId, itemId);
    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, long itemId, Comment text) {
        if (text.getText() == null || text.getText().isBlank()) {
            throw new InvalidPathVariableException("Incorrect comment");
        }
        checkItem(itemId);
        userService.checkUser(userId);
        if (bookingRepository
                .findByUserIdAndStatusIsAndEndDateBeforeOrderByStartDateDesc(userId,
                        BookingStatus.APPROVED, LocalDateTime.now()).isEmpty()) {
            throw new PermissionDeniedException("You have no access to this operation");
        }
        Comment comment = Comment.builder()
                .text(text.getText())
                .user(userService.getUserById(userId))
                .item(itemRepository.findById(itemId).orElseThrow(() -> new InvalidItemIdException(itemId)))
                .created(LocalDateTime.now())
                .build();
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        userService.checkUser(userId);
        return ItemMapper.toItemDto(itemRepository.findById(itemId).orElseThrow(() -> new InvalidItemIdException(itemId)));
    }

    @Override
    public ItemDtoWithCommentsAndBookingInfo getItemByIdWithCommentsAndBookingInfo(long userId, long itemId) {
        userService.checkUser(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new InvalidItemIdException(itemId));
        return ItemMapper.toItemDtoWithCommentsAndBookingInfo(item,
                CommentMapper.toCommentDto(commentRepository.findByItemId(itemId)),
                item.getUser().getId() == userId ?
                    BookingMapper.toBookingDto(bookingRepository
                            .findTopByItemIdAndStartDateBeforeOrderByStartDateDesc(itemId,
                        LocalDateTime.now()).orElse(null)) : null,
                item.getUser().getId() == userId ?
                    BookingMapper.toBookingDto(bookingRepository
                            .findTopByItemIdAndStartDateAfterAndStatusInOrderByStartDateAsc(itemId,
                        LocalDateTime.now(), List.of(BookingStatus.WAITING, BookingStatus.APPROVED)).orElse(null)) : null);
    }

    @Override
    public ItemDto getItemByIdAndUserIdNot(long userId, long itemId) {
        userService.checkUser(userId);
        return ItemMapper.toItemDto(itemRepository
                .findByUserIdNotAndId(userId, itemId).orElseThrow(() -> new InvalidItemIdException(itemId)));
    }

    @Override
    public List<ItemDtoWithBookingInfo> getItemsByUserId(long userId, Integer from, Integer size) {
        userService.checkUser(userId);
        Stream<Item> items;
        if (from == null || size == null) {
            items = itemRepository.findByUserIdOrderByIdAsc(userId).stream();
        } else if (from < 0 || size <= 0) {
            throw new InvalidPathVariableException("Incorrect page parameters");
        } else {
            int pageNumber = from / size;
            final Pageable page = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.ASC, "id"));
            items = itemRepository.findByUserIdOrderByIdAsc(userId, page).get();
        }
        return items.map(item -> ItemMapper.toItemDtoWithBookingInfo(item,
                            item.getUser().getId() == userId ?
                                    BookingMapper.toBookingDto(bookingRepository
                                            .findTopByItemIdAndStartDateBeforeOrderByStartDateDesc(item.getId(),
                                                    LocalDateTime.now()).orElse(null)) : null,
                            item.getUser().getId() == userId ?
                                    BookingMapper.toBookingDto(bookingRepository
                                            .findTopByItemIdAndStartDateAfterAndStatusInOrderByStartDateAsc(item.getId(),
                                                    LocalDateTime.now(),
                                                    List.of(BookingStatus.WAITING, BookingStatus.APPROVED)).orElse(null)) : null))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(long userId, String text, Integer from, Integer size) {
        if (text == null || text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        userService.checkUser(userId);
        if (from == null || size == null) {
            return ItemMapper.toItemDto(itemRepository.searchItemsByText(text, text));
        } else if (from < 0 || size <= 0) {
            throw new InvalidPathVariableException("Incorrect page parameters");
        } else {
            int pageNumber = from / size;
            final Pageable page = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.ASC, "id"));
            return ItemMapper.toItemDto(itemRepository.searchItemsByText(text, text, page).getContent());
        }
/*        if (text == null || text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        userService.checkUser(userId);
        if (from == null || size == null) {
            return ItemMapper.toItemDto(itemRepository
                    .findByAvailableTrueAndUserIdAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(userId,
                            text, text));
        } else if (from < 0 || size <= 0) {
            throw new InvalidPathVariableException("Incorrect page parameters");
        } else {
            int pageNumber = from / size;
            final Pageable page = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.ASC, "id"));
            return ItemMapper.toItemDto(itemRepository
                    .findByAvailableTrueAndUserIdAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(userId,
                            text, text, page).getContent());
        }*/
    }

    @Override
    public void checkItem(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new InvalidItemIdException(itemId);
        }
    }

    private boolean isItemAttrsEmpty(ItemDto itemDto) {
        return itemDto.getDescription() == null || itemDto.getDescription().isBlank() || itemDto.getDescription().isEmpty() ||
                itemDto.getName() == null || itemDto.getName().isBlank() || itemDto.getName().isEmpty() ||
                itemDto.getAvailable() == null;
    }

    private boolean isInvalidId(long id) {
        return id <= 0;
    }

    private boolean isItemExists(long id) {
        return itemRepository.existsById(id);
    }

    private boolean isItemValid(long userId, long id) {
        return !isInvalidId(id) && isItemExists(id);
    }
}