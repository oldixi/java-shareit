package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.InvalidItemRequestIdException;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.exception.InvalidUserIdException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    private final EntityManager em;
    private final ItemRequestService service;

    @Test
    void shouldCreateItemRequestWhenValidRequest() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemRequestDto requestDto = DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви");
        service.createItemRequest(userId, requestDto);

        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.description = :dsc",
                ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("dsc", requestDto.getDescription()).getSingleResult();

        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getCreationDate(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(itemRequest.getUser(), equalTo(user));
    }

    @Test
    void shouldNotCreateItemRequestWhenInvalidUserId() {
        em.persist(UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user")));

        ItemRequestDto requestDto = DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви");

        assertThrows(InvalidUserIdException.class, () -> {
            service.createItemRequest(DtoCreater.INVALID_ID, requestDto);
        });
    }

    @Test
    void shouldGetItemRequestWhenValidIdWithEmptyItems() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemRequest request = ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId,
                        LocalDateTime.now()), user);
        em.persist(request);
        Long requestId = request.getId();

        ItemRequestDtoWithItems requestGet = service.getItemRequestById(userId, requestId);

        assertThat(requestGet.getId(), equalTo(requestId));
        assertThat(requestGet.getCreationDate(), notNullValue());
        assertThat(requestGet.getDescription(), equalTo(request.getDescription()));
        assertThat(requestGet.getUserId(), equalTo(request.getUser().getId()));
        assertThat(requestGet.getItems().size(), equalTo(0));
    }

    @Test
    void shouldNotGetItemRequestByIdWhenInvalidUserId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        ItemRequest request = ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId,
                        LocalDateTime.now()), user);
        em.persist(request);
        Long requestId = request.getId();

        assertThrows(InvalidUserIdException.class, () -> {
            service.getItemRequestById(DtoCreater.INVALID_ID, requestId);
        });
    }

    @Test
    void shouldNotGetItemRequestByIdWhenInvalidId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        em.persist(ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId,
                        LocalDateTime.now()), user));

        assertThrows(InvalidItemRequestIdException.class, () -> {
            service.getItemRequestById(userId, DtoCreater.INVALID_ID);
        });
    }

    @Test
    void shouldGetItemRequestWhenValidId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("userIR@user.com", "userIR"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("ownerIR@user.com", "ownerIR"));
        em.persist(owner);

        ItemRequest request = ItemRequestMapper
                .toItemRequest(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId,
                        LocalDateTime.now()), user);
        em.persist(request);
        Long requestId = request.getId();

        List<ItemDto> items = List.of(
                DtoCreater.makeItemDto("Щетка", "Простая щетка", true, requestId),
                DtoCreater.makeItemDto("Щетка2", "Простая щетка2", true, requestId),
                DtoCreater.makeItemDto("Щетка3", "Простая щетка3", true, requestId));
        items.forEach(itemDto -> em.persist(ItemMapper.toItem(itemDto, owner, request)));

        TypedQuery<Item> query = em.createQuery("Select i from Item i join i.request ir " +
                        "where ir.id = :requestId",
                Item.class);
        List<Item> itemsQuery = query.setParameter("requestId", requestId).getResultList();

        ItemRequestDtoWithItems requestGet = service.getItemRequestById(userId, requestId);

        assertThat(requestGet.getId(), equalTo(requestId));
        assertThat(requestGet.getCreationDate(), notNullValue());
        assertThat(requestGet.getDescription(), equalTo(request.getDescription()));
        assertThat(requestGet.getUserId(), equalTo(request.getUser().getId()));
        assertThat(requestGet.getItems().size(), equalTo(items.size()));
        assertThat(requestGet.getItems().size(), equalTo(itemsQuery.size()));
    }

    @Test
    void shouldNotGetItemRequestsByRequestorIdWhenInvalidRequestorId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        List<ItemRequestDto> requestsDto = List.of(
                DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId, LocalDateTime.now()),
                DtoCreater.makeItemRequestDto("Хотел бы поиграть в PS5", userId, LocalDateTime.now()),
                DtoCreater.makeItemRequestDto("Хотел бы покататься на коньках", userId, LocalDateTime.now()));
        requestsDto.forEach(requestDto -> em.persist(ItemRequestMapper.toItemRequest(requestDto, user)));

        assertThrows(InvalidUserIdException.class, () -> {
            service.getItemRequestsByRequestorId(DtoCreater.INVALID_ID);
        });
    }

    @Test
    void shouldGetItemRequestsWhenValidRequestorId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        List<ItemRequestDto> requestsDto = List.of(
                DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId, LocalDateTime.now()),
                DtoCreater.makeItemRequestDto("Хотел бы поиграть в PS5", userId, LocalDateTime.now()),
                DtoCreater.makeItemRequestDto("Хотел бы покататься на коньках", userId, LocalDateTime.now()));
        requestsDto.forEach(requestDto -> em.persist(ItemRequestMapper.toItemRequest(requestDto, user)));

        List<ItemRequestDtoWithItems> requestsGet = service.getItemRequestsByRequestorId(userId);

        assertThat(requestsGet, hasSize(requestsDto.size()));
        for (ItemRequestDto requestDto : requestsDto) {
            assertThat(requestsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("creationDate", notNullValue()),
                    hasProperty("description", equalTo(requestDto.getDescription())),
                    hasProperty("userId", equalTo(requestDto.getUserId())))));
        }
    }

    @Test
    void shouldGetItemRequestsPageable() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        List<ItemRequestDto> requestsDto = new ArrayList<>(List.of(
                DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId, LocalDateTime.now()),
                DtoCreater.makeItemRequestDto("Хотел бы поиграть в PS5", userId, LocalDateTime.now()),
                DtoCreater.makeItemRequestDto("Хотел бы покататься на коньках", userId, LocalDateTime.now())));
        requestsDto.forEach(requestDto -> em.persist(ItemRequestMapper.toItemRequest(requestDto, owner)));
        Collections.reverse(requestsDto);

        List<ItemRequestDtoWithItems> requestsGet = service.getItemRequests(userId, 2, 2);

        assertThat(requestsGet, hasSize(1));
        for (ItemRequestDto requestDto : requestsDto) {
            assertThat(requestsGet, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("creationDate", notNullValue()),
                    hasProperty("description", equalTo("Хотел бы воспользоваться щёткой для обуви")))));
        }
    }

    @Test
    void shouldGetAllItemRequests() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        List<ItemRequestDto> requestsDto = new ArrayList<>();
        requestsDto.add(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId, LocalDateTime.now()));
        requestsDto.add(DtoCreater.makeItemRequestDto("Хотел бы поиграть в PS5", userId, LocalDateTime.now()));
        requestsDto.add(DtoCreater.makeItemRequestDto("Хотел бы покататься на коньках", userId, LocalDateTime.now()));
        requestsDto.forEach(requestDto -> em.persist(ItemRequestMapper.toItemRequest(requestDto, owner)));
        Collections.reverse(requestsDto);

        List<ItemRequestDtoWithItems> requestsGet = service.getItemRequests(userId, null, null);

        assertThat(requestsGet, hasSize(requestsDto.size()));
        assertThat(requestsGet.stream().map(ItemRequestDtoWithItems::getCreationDate).collect(Collectors.toList()),
                equalToObject(requestsDto.stream().map(ItemRequestDto::getCreationDate).collect(Collectors.toList())));
    }

    @Test
    void shouldNotGetItemRequestsWhenInvalidRequestorId() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        User owner = UserMapper.toUser(DtoCreater.makeUserDto("owner@user.com", "user"));
        em.persist(owner);

        List<ItemRequestDto> requestsDto = new ArrayList<>();
        requestsDto.add(DtoCreater.makeItemRequestDto("Хотел бы воспользоваться щёткой для обуви", userId, LocalDateTime.now()));
        requestsDto.add(DtoCreater.makeItemRequestDto("Хотел бы поиграть в PS5", userId, LocalDateTime.now()));
        requestsDto.add(DtoCreater.makeItemRequestDto("Хотел бы покататься на коньках", userId, LocalDateTime.now()));
        requestsDto.forEach(requestDto -> em.persist(ItemRequestMapper.toItemRequest(requestDto, owner)));
        Collections.reverse(requestsDto);

        assertThrows(InvalidUserIdException.class, () -> {
            service.getItemRequests(DtoCreater.INVALID_ID, null, null);
        });
    }

    @Test
    void shouldGetItemRequestsWhenInvalidPageParameters() {
        User user = UserMapper.toUser(DtoCreater.makeUserDto("user@user.com", "user"));
        em.persist(user);
        Long userId = user.getId();

        assertThrows(InvalidPathVariableException.class, () -> {
            service.getItemRequests(userId, 0, 0);
        });
    }
}