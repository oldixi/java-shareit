package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.InvalidBookingIdException;
import ru.practicum.shareit.exception.InvalidItemIdException;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.exception.InvalidUserIdException;
import ru.practicum.shareit.exception.PermissionDeniedException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private final EntityManager em;
    private final BookingService service;

    @Test
    void createBooking() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, null);

        service.createBooking(userId, bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b",
                Booking.class);
        Booking booking = query.getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStartDate(), equalTo(bookingDto.getStartDate()));
        assertThat(booking.getEndDate(), equalTo(bookingDto.getEndDate()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(booking.getItem(), equalTo(item));
        assertThat(booking.getUser(), equalTo(user));
        assertThat(booking, equalTo(BookingMapper.toBooking(booking.getId(), bookingDto, user, item)));
    }

    @Test
    void createBookingInvalidUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, null);

        assertThrows(InvalidUserIdException.class, () -> {
            service.createBooking(DtoCreater.INVALID_ID, bookingDto);
        });
    }

    @Test
    void createBookingInvalidItemId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), DtoCreater.INVALID_ID, userId, null);

        assertThrows(InvalidItemIdException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void createBookingNotAvailable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", false,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, null);

        assertThrows(PermissionDeniedException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void createBookingStartDateNull() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", false,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(null,
                LocalDateTime.now().plusDays(10), itemId, userId, null);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void createBookingEndDateNull() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", false,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(10),
                null, itemId, userId, null);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void createBookingEqualsStartAndEnd() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", false,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(NOW, NOW, itemId, userId, null);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void createBookingStartBeforeEnd() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", false,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(10), itemId, userId, null);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void createBookingStartBeforeNow() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", false,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                NOW.plusDays(10), itemId, userId, null);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void createBookingEndBeforeNow() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", false,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        BookingDto bookingDto = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                NOW.minusDays(1), itemId, userId, null);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.createBooking(userId, bookingDto);
        });
    }

    @Test
    void updateBookingApproved() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING), user, item);
        em.persist(booking);
        Long bookingId = booking.getId();

        service.updateBooking(ownerId, bookingId, true);
        Booking bookingUpdated = em.find(Booking.class, bookingId);

        assertThat(bookingUpdated.getId(), equalTo(bookingId));
        assertThat(bookingUpdated.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void updateBookingRejected() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING), user, item);
        em.persist(booking);
        Long bookingId = booking.getId();

        service.updateBooking(ownerId, bookingId, false);
        Booking bookingUpdated = em.find(Booking.class, bookingId);

        assertThat(bookingUpdated.getId(), equalTo(bookingId));
        assertThat(bookingUpdated.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void updateBookingInvalidUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING), user, item);
        em.persist(booking);
        Long bookingId = booking.getId();

        assertThrows(InvalidUserIdException.class, () -> {
            service.updateBooking(DtoCreater.INVALID_ID, bookingId, true);
        });
    }

    @Test
    void updateBookingInvalidId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING), user, item);
        em.persist(booking);

        assertThrows(InvalidBookingIdException.class, () -> {
            service.updateBooking(ownerId, DtoCreater.INVALID_ID, true);
        });
    }

    @Test
    void updateBookingInvalidApproving() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(booking);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.updateBooking(ownerId, booking.getId(), true);
        });
    }

    @Test
    void updateBookingInvalidRejecting() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED), user, item);
        em.persist(booking);

        assertThrows(InvalidPathVariableException.class, () -> {
            service.updateBooking(ownerId, booking.getId(), false);
        });
    }

    @Test
    void getBookingById() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING), user, item);
        em.persist(booking);
        Long bookingId = booking.getId();

        Booking bookingGet = service.getBookingById(ownerId, bookingId);

        assertThat(bookingGet.getId(), equalTo(bookingId));
        assertThat(bookingGet.getStartDate(), equalTo(booking.getStartDate()));
        assertThat(bookingGet.getEndDate(), equalTo(booking.getEndDate()));
        assertThat(bookingGet.getStatus(), equalTo(booking.getStatus()));
        assertThat(bookingGet.getUser(), equalTo(booking.getUser()));
        assertThat(bookingGet.getItem(), equalTo(booking.getItem()));
    }

    @Test
    void getBookingByIdInvalidId() {
        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        assertThrows(InvalidBookingIdException.class, () -> {
            service.getBookingById(ownerId, DtoCreater.INVALID_ID);
        });
    }

    @Test
    void getBookingInvalidUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING), user, item);
        em.persist(booking);
        Long bookingId = booking.getId();

        assertThrows(InvalidUserIdException.class, () -> {
            service.getBookingById(DtoCreater.INVALID_ID, bookingId);
        });
    }

    @Test
    void getBookingOtherUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        User otherUser = UserMapper.toUser(DtoCreater.makeUserDto("other@user.com", "other"));
        em.persist(otherUser);
        Long otherId = otherUser.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING), user, item);
        em.persist(booking);
        Long bookingId = booking.getId();

        assertThrows(InvalidBookingIdException.class, () -> {
            service.getBookingById(otherId, bookingId);
        });
    }

    @Test
    void getBookingByStateWaiting() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.WAITING), null, null);

        assertThat(bookingsGet, hasSize(1));
        for (BookingDto bookingDto : bookingsDto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))));
        }
    }

    @Test
    void getBookingByStateRejected() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.REJECTED), null, null);

        assertThat(bookingsGet, hasSize(2));
        for (BookingDto bookingDto : bookingsDto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.REJECTED)))));
        }
    }

    @Test
    void getBookingByStateBlank() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, null, null, null);

        assertThat(bookingsGet, hasSize(5));
    }

    @Test
    void getBookingByStateAll() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.ALL), null, null);

        assertThat(bookingsGet, hasSize(5));
    }

    @Test
    void getBookingByStateCurrent() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.CURRENT), null, null);

        assertThat(bookingsGet, hasSize(1));
    }

    @Test
    void getBookingByStateFuture() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.FUTURE), null, null);

        assertThat(bookingsGet, hasSize(3));
    }

    @Test
    void getBookingByStatePast() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.PAST), null, null);

        assertThat(bookingsGet, hasSize(1));
    }

    @Test
    void getBookingByStateInvalidState() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.getBookingByState(userId, "String.valueOf(BookingState.ALL)", null, null);
        });
    }

    @Test
    void getBookingByStateAllPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.ALL), 2, 2);

        assertThat(bookingsGet, hasSize(2));
    }

    @Test
    void getBookingByStatePastPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.PAST), 0, 10);

        assertThat(bookingsGet, hasSize(1));
    }

    @Test
    void getBookingByStateFuturePageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.FUTURE), 2, 2);

        assertThat(bookingsGet, hasSize(1));
    }

    @Test
    void getBookingByStateCurrentPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.CURRENT), 1, 2);

        assertThat(bookingsGet, hasSize(1));
    }

    @Test
    void getBookingByStateRejectedPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.REJECTED), 0, 2);

        assertThat(bookingsGet, hasSize(2));
        for (BookingDto bookingDto : bookingsDto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.REJECTED)))));
        }
    }

    @Test
    void getBookingByStateWaitingPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        List<BookingDto> bookingsDto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), itemId, userId, BookingStatus.REJECTED));

        bookingsDto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item)));

        List<Booking> bookingsGet = service.getBookingByState(userId, String.valueOf(BookingState.WAITING), 0, 2);

        assertThat(bookingsGet, hasSize(1));
        for (BookingDto bookingDto : bookingsDto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))));
        }
    }

    @Test
    void getBookingByStateInvalidPageParameters() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.getBookingByState(userId, String.valueOf(BookingState.WAITING), 0, 0);
        });
    }

    @Test
    void getBookingByStateAndOwnerWaiting() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.WAITING), null, null);

        assertThat(bookingsGet, hasSize(1));
        for (BookingDto bookingDto : bookingsItem1Dto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))));
        }
    }

    @Test
    void getBookingByStateAndOwnerRejected() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.REJECTED), null, null);

        assertThat(bookingsGet, hasSize(1));
        for (BookingDto bookingDto : bookingsItem1Dto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.REJECTED)))));
        }
    }

    @Test
    void getBookingByStateAndOwnerBlank() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId, null, null, null);

        assertThat(bookingsGet, hasSize(3));
    }

    @Test
    void getBookingByStateAndOwnerAll() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.ALL), null, null);

        assertThat(bookingsGet, hasSize(3));
    }

    @Test
    void getBookingByStateAndOwnerCurrent() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.CURRENT), null, null);

        assertThat(bookingsGet, hasSize(0));
    }

    @Test
    void getBookingByStateAndOwnerFuture() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.FUTURE), null, null);

        assertThat(bookingsGet, hasSize(3));
    }

    @Test
    void getBookingByStateAndOwnerPast() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.PAST), null, null);

        assertThat(bookingsGet, hasSize(0));
    }

    @Test
    void getBookingByStateAndOwnerInvalidState() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.getBookingsByOwnerAndState(ownerId, "String.valueOf(BookingState.ALL)", null, null);
        });
    }

    @Test
    void getBookingByStateAndOwnerWaitingPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.WAITING), 0, 2);

        assertThat(bookingsGet, hasSize(1));
        for (BookingDto bookingDto : bookingsItem1Dto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))));
        }
    }

    @Test
    void getBookingByStateAndOwnerRejectedPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.REJECTED), 0, 1);

        assertThat(bookingsGet, hasSize(1));
        for (BookingDto bookingDto : bookingsItem1Dto) {
            assertThat(bookingsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(BookingStatus.REJECTED)))));
        }
    }

    @Test
    void getBookingByStateAndOwnerAllPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.ALL), 2, 2);

        assertThat(bookingsGet, hasSize(1));
    }

    @Test
    void getBookingByStateAndOwnerCurrentPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.CURRENT), 0, 2);

        assertThat(bookingsGet, hasSize(0));
    }

    @Test
    void getBookingByStateAndOwnerFuturePageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.FUTURE), 1, 2);

        assertThat(bookingsGet, hasSize(2));
    }

    @Test
    void getBookingByStateAndOwnerPastPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель2", "Простая дрель2", true,
                null), user);
        em.persist(item2);
        Long item2Id = item.getId();

        List<BookingDto> bookingsItem1Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.WAITING),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(10), itemId, userId, BookingStatus.REJECTED));

        List<BookingDto> bookingsItem2Dto = List.of(
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(10), item2Id, ownerId, BookingStatus.APPROVED),
                DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15),
                        LocalDateTime.now().minusDays(10), item2Id, ownerId, BookingStatus.REJECTED));

        bookingsItem1Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, owner, item)));
        bookingsItem2Dto.forEach(bookingDto -> em.persist(BookingMapper.toBooking(bookingDto, user, item2)));

        List<Booking> bookingsGet = service.getBookingsByOwnerAndState(ownerId,
                String.valueOf(BookingState.PAST), 1, 5);

        assertThat(bookingsGet, hasSize(0));
    }

    @Test
    void getBookingByStateAndOwnerInvalidPageParameters() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.getBookingsByOwnerAndState(userId, String.valueOf(BookingState.WAITING), 0, 0);
        });
    }

    @Test
    void getBookingByStateAndOwnerInvalidStatePageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.getBookingsByOwnerAndState(userId, "String.valueOf(BookingState.WAITING)", 0, 1);
        });
    }
}