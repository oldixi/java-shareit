package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Item createItem(long userId, ItemDto itemDto) {
        if (isItemAttrsEmpty(itemDto)) {
            throw new InvalidItemAttrsException();
        }
        User user = userService.getUserById(userId);
        return itemRepository.save(ItemMapper.toItem(itemDto, user));
    }

    @Override
    @Transactional
    public Item updateItem(long userId, long itemId, ItemDto itemDto) {
        if (!isItemValid(userId, itemId)) {
            throw new InvalidItemIdException(itemId);
        }
        User user = userService.getUserById(userId);
        return itemRepository.save(ItemMapper.toItem(itemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new InvalidItemIdException(itemId)), itemDto, user));
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
    public ItemDtoWithCommentsAndBookingInfo getItemByIdAndUserId(long itemId, long userId) {
        userService.checkUser(userId);
        return ItemMapper.toItemDtoWithCommentsAndBookingInfo(
                itemRepository.findByUserIdAndId(itemId, userId).orElseThrow(() -> new InvalidItemIdException(itemId)),
                CommentMapper.toCommentDto(commentRepository.findByUserIdAndItemId(userId, itemId)),
                BookingMapper.toBookingDto(bookingRepository
                        .findTopByItemIdAndStartDateBeforeOrderByStartDateDesc(itemId,
                                LocalDateTime.now()).orElse(null)),
                BookingMapper.toBookingDto(bookingRepository
                        .findTopByItemIdAndStartDateAfterAndStatusInOrderByStartDateAsc(itemId,
                                LocalDateTime.now(),
                                List.of(BookingStatus.WAITING, BookingStatus.APPROVED)).orElse(null)));
    }

    @Override
    public ItemDto getItemByIdAndUserIdNot(long userId, long itemId) {
        userService.checkUser(userId);
        return ItemMapper.toItemDto(itemRepository
                .findByUserIdNotAndId(userId, itemId).orElseThrow(() -> new InvalidItemIdException(itemId)));
    }

    @Override
    public List<ItemDtoWithBookingInfo> getItemsByUserId(long userId) {
        userService.checkUser(userId);
        return itemRepository.findByUserId(userId).stream()
                .map(item -> ItemMapper.toItemDtoWithBookingInfo(item,
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
    public List<ItemDto> searchItems(long userId, String text) {
        if (text == null || text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        userService.checkUser(userId);
        return ItemMapper.toItemDto(itemRepository
                .findByAvailableTrueAndUserIdAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(userId,
                        text, text));
    }

    @Override
    public void checkItem(long userId, long itemId) {
        if (!isItemValid(userId, itemId)) {
            throw new InvalidItemIdException(itemId);
        }
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

    private boolean isItemExists(long userId, long id) {
        return itemRepository.existsById(id) && itemRepository.findByUserIdAndId(userId, id).isPresent();
    }

    private boolean isItemValid(long userId, long id) {
        return !isInvalidId(id) && isItemExists(userId, id);
    }
}