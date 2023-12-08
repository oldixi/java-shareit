package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.exception.InvalidItemAttrsException;
import ru.practicum.shareit.exception.InvalidItemIdException;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.exception.InvalidUserIdException;
import ru.practicum.shareit.exception.PermissionDeniedException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookingInfo;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToObject;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {
    private final EntityManager em;
    private final ItemService service;

    @Test
    void createItemWithoutRequest() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemDto itemDto = DtoCreater.makeItemDto("Дрель", "Простая дрель", true, null);
        service.createItem(userId, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name",
                Item.class);
        Item item = query.setParameter("name", itemDto.getName()).getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.isAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getRequest(), nullValue());
        assertThat(item.getUser(), equalTo(user));
    }

    @Test
    void createItemInvalidUserId() {
        em.persist(UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user")));

        ItemDto itemDto = DtoCreater.makeItemDto("Дрель", "Простая дрель", true, null);

        assertThrows(InvalidUserIdException.class, () -> {
            service.createItem(DtoCreater.INVALID_ID, itemDto);
        });
    }

    @Test
    void createItemNullDescription() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemDto itemDto = DtoCreater.makeItemDto("Дрель", null, true, null);

        assertThrows(InvalidItemAttrsException.class, () -> {
            service.createItem(userId, itemDto);
        });
    }

    @Test
    void createItemEmptyDescription() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemDto itemDto = DtoCreater.makeItemDto("Дрель", "", true, null);

        assertThrows(InvalidItemAttrsException.class, () -> {
            service.createItem(userId, itemDto);
        });
    }

    @Test
    void createItemBlankDescription() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemDto itemDto = DtoCreater.makeItemDto("Дрель", "  ", true, null);

        assertThrows(InvalidItemAttrsException.class, () -> {
            service.createItem(userId, itemDto);
        });
    }

    @Test
    void createItemNullName() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemDto itemDto = DtoCreater.makeItemDto(null, "Дрель обыкновенная", false, null);

        assertThrows(InvalidItemAttrsException.class, () -> {
            service.createItem(userId, itemDto);
        });
    }

    @Test
    void createItemEmptyName() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemDto itemDto = DtoCreater.makeItemDto("", "Дрель обыкновенная", false, null);

        assertThrows(InvalidItemAttrsException.class, () -> {
            service.createItem(userId, itemDto);
        });
    }

    @Test
    void createItemBlankName() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemDto itemDto = DtoCreater.makeItemDto("  ", "Дрель обыкновенная", false, null);

        assertThrows(InvalidItemAttrsException.class, () -> {
            service.createItem(userId, itemDto);
        });
    }

    @Test
    void createItemWithRequest() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemRequest request = ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться дрелью", userId,
                        LocalDateTime.now()), user);
        em.persist(request);
        Long requestId = request.getId();

        ItemDto itemDto = DtoCreater.makeItemDto("Дрель", "Простая дрель", true, requestId);
        service.createItem(userId, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name",
                Item.class);
        Item item = query.setParameter("name", itemDto.getName()).getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.isAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getRequest(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("creationDate", notNullValue()),
                hasProperty("description", equalTo(request.getDescription())),
                hasProperty("user", equalTo(user))));
        assertThat(item.getUser(), equalTo(user));
    }

    @Test
    void updateItemWithRequest() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemRequest request = ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться дрелью", userId,
                        LocalDateTime.now()), user);
        em.persist(request);
        Long requestId = request.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                requestId), user);
        em.persist(item);
        Long itemId = item.getId();

        ItemDto itemDtoUpdated = DtoCreater.makeItemDto("ДрельUpdated", "Непростая дрель", false,
                requestId);
        service.updateItem(userId, itemId, itemDtoUpdated);

        Item itemUpdated = em.find(Item.class, itemId);

        assertThat(itemUpdated.getId(), notNullValue());
        assertThat(itemUpdated.getName(), equalTo(itemDtoUpdated.getName()));
        assertThat(itemUpdated.isAvailable(), equalTo(itemDtoUpdated.getAvailable()));
        assertThat(itemUpdated.getDescription(), equalTo(itemDtoUpdated.getDescription()));
        assertThat(item.getRequest(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("creationDate", notNullValue()),
                hasProperty("description", equalTo(request.getDescription())),
                hasProperty("user", equalTo(user))));
        assertThat(itemUpdated.getUser(), equalTo(user));
    }

    @Test
    void updateItemIvalidUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemRequest request = ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться дрелью", userId,
                        LocalDateTime.now()), user);
        em.persist(request);
        Long requestId = request.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                requestId), user);
        em.persist(item);
        Long itemId = item.getId();

        ItemDto itemDtoUpdated = DtoCreater.makeItemDto("ДрельUpdated", "Непростая дрель", false,
                requestId);

        assertThrows(InvalidUserIdException.class, () -> {
            service.updateItem(DtoCreater.INVALID_ID, itemId, itemDtoUpdated);
        });
    }

    @Test
    void updateItemIvalidItemId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemRequest request = ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться дрелью", userId,
                        LocalDateTime.now()), user);
        em.persist(request);
        Long requestId = request.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                requestId), user);
        em.persist(item);
        Long itemId = item.getId();

        ItemDto itemDtoUpdated = DtoCreater.makeItemDto("ДрельUpdated", "Непростая дрель", false,
                requestId);

        assertThrows(InvalidItemIdException.class, () -> {
            service.updateItem(userId, DtoCreater.INVALID_ID, itemDtoUpdated);
        });
    }

    @Test
    void updateItemWithoutRequest() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), user);
        em.persist(item);
        Long itemId = item.getId();

        ItemDto itemDtoUpdated = DtoCreater.makeItemDto("ДрельUpdated", "Непростая дрель", false,
                null);
        service.updateItem(userId, itemId, itemDtoUpdated);

        Item itemUpdated = em.find(Item.class, itemId);

        assertThat(itemUpdated.getId(), notNullValue());
        assertThat(itemUpdated.getName(), equalTo(itemDtoUpdated.getName()));
        assertThat(itemUpdated.isAvailable(), equalTo(itemDtoUpdated.getAvailable()));
        assertThat(itemUpdated.getDescription(), equalTo(itemDtoUpdated.getDescription()));
        assertThat(itemUpdated.getRequest(), nullValue());
        assertThat(itemUpdated.getUser(), equalTo(user));
    }

    @Test
    void deleteItem() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), user);
        em.persist(item);
        Long itemId = item.getId();

        service.deleteItem(userId, itemId);
        Item itemDeleted = em.find(Item.class, itemId);

        assertThat(itemDeleted, nullValue());
    }

    @Test
    void getItemById() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), user);
        em.persist(item);
        Long itemId = item.getId();

        ItemDto itemGet = service.getItemById(userId, itemId);

        assertThat(itemGet.getId(), notNullValue());
        assertThat(itemGet.getName(), equalTo(item.getName()));
        assertThat(itemGet.getDescription(), equalTo(item.getDescription()));
        assertThat(itemGet.getAvailable(), equalTo(item.isAvailable()));
        assertThat(itemGet.getRequestId(), nullValue());
    }

    @Test
    void getItemByIdInvalidUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), user);
        em.persist(item);
        Long itemId = item.getId();

        assertThrows(InvalidUserIdException.class, () -> {
            service.getItemById(DtoCreater.INVALID_ID, itemId);
        });
    }

    @Test
    void getItemByIdInvalidId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), user);
        em.persist(item);

        assertThrows(InvalidItemIdException.class, () -> {
            service.getItemById(userId, DtoCreater.INVALID_ID);
        });
    }

    @Test
    void createItemComments() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1), itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(booking);

        CommentDto commentDto = DtoCreater.makeCommentDto("Отличная дрель", "user", LocalDateTime.now());
        service.createComment(userId, itemId, CommentMapper.toComment(commentDto, user, item));

        TypedQuery<Comment> query = em.createQuery("Select c from Comment c where c.text = :text",
                Comment.class);
        Comment comment = query.setParameter("text", "Отличная дрель").getSingleResult();

        assertThat(comment.getId(), notNullValue());
        assertThat(comment.getText(), equalTo(commentDto.getText()));
        assertThat(comment.getCreated(), notNullValue());
        assertThat(comment.getUser(), hasProperty("name", equalTo(commentDto.getAuthorName())));
        assertThat(comment.getItem(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.isAvailable()))));
    }

    @Test
    void createItemCommentsPermissionDeniedWrongBookerId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1), itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(booking);

        CommentDto commentDto = DtoCreater.makeCommentDto("Отличная дрель", "user", LocalDateTime.now());

        assertThrows(PermissionDeniedException.class, () -> {
            service.createComment(ownerId, itemId, CommentMapper.toComment(commentDto, owner, item));
        });
    }

    @Test
    void createItemCommentsPermissionDeniedNotApprovedBooking() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1), itemId, userId, BookingStatus.REJECTED), user, item);
        em.persist(booking);

        CommentDto commentDto = DtoCreater.makeCommentDto("Отличная дрель", "user", LocalDateTime.now());

        assertThrows(PermissionDeniedException.class, () -> {
            service.createComment(userId, itemId, CommentMapper.toComment(commentDto, user, item));
        });
    }

    @Test
    void createItemCommentsPermissionDeniedNotClosedBooking() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(1), itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(booking);

        CommentDto commentDto = DtoCreater.makeCommentDto("Отличная дрель", "user", LocalDateTime.now());

        assertThrows(PermissionDeniedException.class, () -> {
            service.createComment(userId, itemId, CommentMapper.toComment(commentDto, user, item));
        });
    }

    @Test
    void createItemCommentsInvalidUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1), itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(booking);

        CommentDto commentDto = DtoCreater.makeCommentDto("Отличная дрель", "user", LocalDateTime.now());

        assertThrows(InvalidUserIdException.class, () -> {
            service.createComment(DtoCreater.INVALID_ID, itemId, CommentMapper.toComment(commentDto, user, item));
        });
    }

    @Test
    void createItemCommentsInvalidId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1), itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(booking);

        CommentDto commentDto = DtoCreater.makeCommentDto("Отличная дрель", "user", LocalDateTime.now());

        assertThrows(InvalidItemIdException.class, () -> {
            service.createComment(userId, DtoCreater.INVALID_ID, CommentMapper.toComment(commentDto, user, item));
        });
    }

    @Test
    void createItemCommentsNullText() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking booking = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1), itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(booking);

        CommentDto commentDto = DtoCreater.makeCommentDto(null, "user", LocalDateTime.now());

        assertThrows(InvalidPathVariableException.class, () -> {
            service.createComment(userId, itemId, CommentMapper.toComment(commentDto, user, item));
        });
    }

    @Test
    void getItemByIdAndUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        Booking bookingCurrent = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(12),
                itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(bookingCurrent);

        Booking bookingLast = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(6),
                itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(bookingLast);

        Booking bookingNext = BookingMapper.toBooking(DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(10),
                itemId, userId, BookingStatus.APPROVED), user, item);
        em.persist(bookingNext);

        CommentDto commentDto = DtoCreater.makeCommentDto("Отличная дрель", "user", LocalDateTime.now());
        em.persist(CommentMapper.toComment(commentDto, user, item));

        ItemDtoWithCommentsAndBookingInfo itemGet = service.getItemByIdWithCommentsAndBookingInfo(ownerId, itemId);

        TypedQuery<Comment> query = em.createQuery("Select c from Comment c join c.item i where i.id = :id",
                Comment.class);
        List<Comment> comments = query.setParameter("id", itemId).getResultList();

        assertThat(itemGet.getId(), notNullValue());
        assertThat(itemGet.getName(), equalTo(item.getName()));
        assertThat(itemGet.getDescription(), equalTo(item.getDescription()));
        assertThat(itemGet.getAvailable(), equalTo(item.isAvailable()));
        assertThat(itemGet.getComments(), hasSize(1));
        assertThat(itemGet.getComments()
                .stream()
                .map(commentDtoFromComments -> CommentMapper
                        .toComment(commentDtoFromComments.getId(), commentDto, user, item))
                .collect(Collectors.toList()), equalTo(comments));
        assertThat(itemGet.getLastBooking(), equalTo(BookingMapper.toBookingDto(bookingLast)));
        assertThat(itemGet.getNextBooking(), equalTo(BookingMapper.toBookingDto(bookingNext)));
    }

    @Test
    void getItemByIdAndUserIdInvalidUserId() {
        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        assertThrows(InvalidUserIdException.class, () -> {
            service.getItemByIdWithCommentsAndBookingInfo(DtoCreater.INVALID_ID, itemId);
        });
    }

    @Test
    void getItemByIdAndUserIdInvalidId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        assertThrows(InvalidItemIdException.class, () -> {
            service.getItemByIdWithCommentsAndBookingInfo(userId, DtoCreater.INVALID_ID);
        });
    }

    @Test
    void getItemByIdAndUserIdNot() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        ItemDto itemGet = service.getItemByIdAndUserIdNot(userId, itemId);

        assertThat(itemGet.getId(), notNullValue());
        assertThat(itemGet.getName(), equalTo(item.getName()));
        assertThat(itemGet.getDescription(), equalTo(item.getDescription()));
        assertThat(itemGet.getAvailable(), equalTo(item.isAvailable()));
    }

    @Test
    void getItemByIdAndUserIdNotInvalidUserId() {
        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        Item item = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item);
        Long itemId = item.getId();

        assertThrows(InvalidUserIdException.class, () -> {
            service.getItemByIdAndUserIdNot(DtoCreater.INVALID_ID, itemId);
        });
    }

    @Test
    void getItemByIdAndUserIdNotInvalidId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        assertThrows(InvalidItemIdException.class, () -> {
            service.getItemByIdAndUserIdNot(userId, DtoCreater.INVALID_ID);
        });
    }

    @Test
    void getItemsByUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item1 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item1);
        Long itemId = item1.getId();

        Item item2 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель аккумуляторная", "Аккумуляторная дрель",
                true, null), owner);
        em.persist(item2);
        List<Item> items = List.of(item1, item2);

        BookingDto bookingCurrent = DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(12),
                itemId, userId, BookingStatus.APPROVED);
        em.persist(BookingMapper.toBooking(bookingCurrent, user, item1));

        BookingDto bookingLast = DtoCreater.makeBookingDto(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(6),
                itemId, userId, BookingStatus.APPROVED);
        em.persist(BookingMapper.toBooking(bookingLast, user, item1));

        BookingDto bookingNext = DtoCreater.makeBookingDto(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(10),
                itemId, userId, BookingStatus.APPROVED);
        em.persist(BookingMapper.toBooking(bookingNext, user, item1));

        List<ItemDtoWithBookingInfo> itemsGet = service.getItemsByUserId(ownerId, null, null);

        assertThat(itemsGet, hasSize(items.size()));
        assertThat(itemsGet.stream().map(ItemDtoWithBookingInfo::getName).collect(Collectors.toList()),
                equalToObject(items.stream().map(Item::getName).collect(Collectors.toList())));
        assertThat(itemsGet.stream().map(ItemDtoWithBookingInfo::getDescription).collect(Collectors.toList()),
                equalToObject(items.stream().map(Item::getDescription).collect(Collectors.toList())));
        assertThat(itemsGet.stream().filter(item -> item.getLastBooking() != null)
                        .map(item -> item.getLastBooking().getStartDate()).collect(Collectors.toList()),
                hasItem(equalToObject(bookingLast.getStartDate())));
        assertThat(itemsGet.stream().filter(item -> item.getLastBooking() != null)
                        .map(item -> item.getLastBooking().getEndDate()).collect(Collectors.toList()),
                hasItem(equalToObject(bookingLast.getEndDate())));
        assertThat(itemsGet.stream().filter(item -> item.getLastBooking() != null)
                        .map(item -> item.getNextBooking().getStartDate()).collect(Collectors.toList()),
                hasItem(equalToObject(bookingNext.getStartDate())));
        assertThat(itemsGet.stream().filter(item -> item.getLastBooking() != null)
                        .map(item -> item.getNextBooking().getEndDate()).collect(Collectors.toList()),
                hasItem(equalToObject(bookingNext.getEndDate())));
    }

    @Test
    void getItemsByUserIdInvalidUserId() {
        assertThrows(InvalidUserIdException.class, () -> {
            service.getItemsByUserId(DtoCreater.INVALID_ID, null, null);
        });
    }

    @Test
    void getItemsByUserIdPageable() {
        em.persist(UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user")));

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        Item item1 = ItemMapper.toItem(DtoCreater.makeItemDto("Дрель", "Простая дрель", true,
                null), owner);
        em.persist(item1);

        em.persist(ItemMapper.toItem(DtoCreater.makeItemDto("Дрель аккумуляторная", "Аккумуляторная дрель",
                true, null), owner));

        List<ItemDtoWithBookingInfo> itemsGet = service.getItemsByUserId(ownerId, 0, 1);

        assertThat(itemsGet, hasSize(1));
        assertThat(itemsGet, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("description", equalTo(item1.getDescription())),
                hasProperty("available", equalTo(item1.isAvailable())))));
    }

    @Test
    void getItemsByUserIdPageableInvalidPageParameters() {
        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.getItemsByUserId(ownerId, 0, 0);
        });
    }

    @Test
    void searchItemsEmpty() {
        em.persist(UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user")));

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        List<ItemDto> itemsGet = service.searchItems(ownerId, null, null, null);

        assertThat(itemsGet, hasSize(0));
    }

    @Test
    void searchItemsInvalidUserId() {
        assertThrows(InvalidUserIdException.class, () -> {
            service.searchItems(DtoCreater.INVALID_ID, "АккУм", null, null);
        });
    }

    @Test
    void searchItemsPageableInvalidPageParameters() {
        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "owner"));
        em.persist(owner);
        Long ownerId = owner.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.searchItems(ownerId, "АккУм", 0, 0);
        });
    }
}