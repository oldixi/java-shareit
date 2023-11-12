package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.InvalidBookingIdException;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.exception.PermissionDeniedException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    @Lazy
    private final ItemService itemService;
    private final UserService userService;

    @Override
    @Transactional
    public Booking createBooking(long userId, BookingDto bookingDto) {
        if (isNewBookingDatesInvalid(bookingDto.getStartDate(), bookingDto.getEndDate())) {
            throw new InvalidPathVariableException("Wrong dates in booking request");
        }
        userService.checkUser(userId);
        itemService.checkItem(bookingDto.getItemId());
        User user = userService.getUserById(userId);
        Item item = ItemMapper.toItem(bookingDto.getItemId(),
                itemService.getItemByIdAndUserIdNot(userId, bookingDto.getItemId()), user);
        if (!item.isAvailable()) {
            throw new PermissionDeniedException("Item is not available");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(BookingMapper.toBooking(bookingDto, user, item));
    }

    @Override
    @Transactional
    public Booking updateBooking(long userId, long bookingId, boolean approved) {
        if (!isBookingValid(bookingId)) {
            throw new InvalidBookingIdException(bookingId);
        }
        userService.checkUser(userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new InvalidBookingIdException(bookingId));
        itemService.checkItem(userId, booking.getItem().getId());
        if (approved && booking.getStatus().equals(BookingStatus.APPROVED) ||
            !approved && booking.getStatus().equals(BookingStatus.REJECTED)) {
            throw new InvalidPathVariableException("Status of booking is already set");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(long userId, long bookingId) {
        userService.checkUser(userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new InvalidBookingIdException(bookingId));
        if (booking.getUser().getId() != userId && booking.getItem().getUser().getId() != userId) {
            throw new InvalidBookingIdException(bookingId);
        }
        return booking;
    }

    @Override
    public List<Booking> getBookingByState(long userId, String state) {
        userService.checkUser(userId);
        if (state == null || state.isBlank() || state.isEmpty()) {
            state = BookingState.ALL.toString();
        }
        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    return bookingRepository.findByUserIdOrderByStartDateDesc(userId);
                case WAITING:
                case REJECTED:
                    return bookingRepository.findByUserIdAndStatusIsOrderByStartDateDesc(userId,
                            BookingStatus.valueOf(state));
                case FUTURE:
                    return bookingRepository
                            .findByUserIdAndStartDateAfterOrderByStartDateDesc(userId, LocalDateTime.now());
                case PAST:
                    return bookingRepository
                            .findByUserIdAndEndDateBeforeOrderByStartDateDesc(userId, LocalDateTime.now());
                case CURRENT:
                    return bookingRepository
                            .findByUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(userId,
                                    LocalDateTime.now(), LocalDateTime.now());
                default:
                    throw new InvalidPathVariableException("Unknown state: " + state);
            }
        } catch (IllegalArgumentException iae) {
            throw new InvalidPathVariableException("Unknown state: " + state);
        }
    }

    @Override
    public List<Booking> getBookingsByOwnerAndState(long userId, String state) {
        userService.checkUser(userId);
        if (state == null || state.isBlank() || state.isEmpty()) {
            state = BookingState.ALL.toString();
        }
        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    return bookingRepository.findByItemUserIdOrderByStartDateDesc(userId);
                case WAITING:
                case REJECTED:
                    return bookingRepository.findByItemUserIdAndStatusIsOrderByStartDateDesc(userId,
                            BookingStatus.valueOf(state));
                case FUTURE:
                    return bookingRepository
                            .findByItemUserIdAndStartDateAfterOrderByStartDateDesc(userId, LocalDateTime.now());
                case PAST:
                    return bookingRepository
                            .findByItemUserIdAndEndDateBeforeOrderByStartDateDesc(userId, LocalDateTime.now());
                case CURRENT:
                    return bookingRepository
                            .findByItemUserIdAndEndDateAfterAndStartDateBeforeOrderByIdAsc(userId,
                                    LocalDateTime.now(), LocalDateTime.now());
                default:
                    throw new InvalidPathVariableException("Unknown state: " + state);
            }
        } catch (IllegalArgumentException iae) {
            throw new InvalidPathVariableException("Unknown state: " + state);
        }
    }

    public Booking getLastBookingByItemId(long itemId) {
        itemService.checkItem(itemId);
        return bookingRepository.findTopByItemIdAndStartDateBeforeOrderByStartDateDesc(itemId,
                LocalDateTime.now()).orElse(new Booking());
    }

    public Booking getNextBookingByItemId(long itemId) {
        itemService.checkItem(itemId);
        return bookingRepository.findTopByItemIdAndStartDateAfterAndStatusInOrderByStartDateAsc(itemId,
                LocalDateTime.now(), List.of(BookingStatus.WAITING, BookingStatus.APPROVED)).orElse(new Booking());
    }

    private boolean isInvalidId(long id) {
        return id <= 0;
    }

    private boolean isBookingExists(long id) {
        return bookingRepository.existsById(id);
    }

    private boolean isNewBookingDatesInvalid(LocalDateTime bookingStartDate, LocalDateTime bookingEndDate) {
        return bookingStartDate == null || bookingEndDate == null
                || bookingStartDate.isEqual(bookingEndDate)
                || bookingEndDate.isBefore(bookingStartDate)
                || bookingStartDate.isBefore(LocalDateTime.now())
                || bookingEndDate.isBefore(LocalDateTime.now());
    }

    public boolean isBookingValid(long id) {
        return !isInvalidId(id) && isBookingExists(id);
    }
}