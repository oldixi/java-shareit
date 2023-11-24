package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingInfo;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    private static final LocalDateTime START = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime END = LocalDateTime.now().minusDays(1);

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private final ItemDto itemDto = ItemDto.builder()
            .description("Отвертка аккум")
            .name("Отвертка")
            .available(true)
            .requestId(1L)
            .build();

    private final ItemDto item2Dto = ItemDto.builder()
            .description("Дрель аккум")
            .name("Дрель")
            .available(true)
            .build();

    private final ItemDto itemUpdatedDto = ItemDto.builder()
            .description("Простая отвертка")
            .name("Вторая отвертка")
            .available(true)
            .requestId(1L)
            .build();

    private final BookingDto lastBookingDto = BookingDto.builder()
            .id(1L)
            .itemId(1L)
            .userId(1L)
            .status(BookingStatus.APPROVED)
            .startDate(START)
            .endDate(END)
            .build();

    private final BookingDto nextBookingDto = BookingDto.builder()
            .id(1L)
            .itemId(1L)
            .userId(1L)
            .status(BookingStatus.APPROVED)
            .startDate(START)
            .endDate(END)
            .build();

    private final List<CommentDto> comments = List.of(CommentDto.builder()
            .id(1L)
            .authorName("user")
            .text("Отличная отвертка. Спасибо.")
            .created(START)
            .build());

    private CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .authorName("user")
            .text("Отличная отвертка. Спасибо.")
            .created(START)
            .build();

    @Test
    void createItemRequestServiceItem() throws Exception {
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(getItemRequestList().get(0));

        when(itemRequestService.createItemRequest(anyLong(), any()))
                .thenReturn(getItemRequestList().get(0));

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));
    }

    @Test
    void createItemRequestNullDescription() throws Exception {
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(getItemRequestList().get(0));

        when(itemRequestService.createItemRequest(anyLong(), any()))
                .thenThrow(InvalidPathVariableException.class);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequests() throws Exception {
        when(itemRequestService.getItemRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(getItemRequestWithItemsList());

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestsByUser() throws Exception {
        when(itemRequestService.getItemRequestsByRequestorId(anyLong()))
                .thenReturn(getItemRequestWithItemsList());

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestById() throws Exception {
        ItemRequestDtoWithItems request = ItemRequestMapper.toItemRequestDtoWithItems(getItemRequestList().get(0),
                List.of(itemDto));

        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(request);

        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.description", is(request.getDescription())));
    }

    private List<User> getUsersList() {
        User user1 = User.builder().id(1L).email("user1@user").name("user1").build();
        User user2 = User.builder().id(2L).email("user2@user").name("user2").build();
        return List.of(user1, user2);
    }

    private List<Item> getItemsList() {
        Item item1 = Item.builder()
                .id(1L)
                .description("Обычная отвертка")
                .name("Отвертка 1")
                .available(true)
                .request(getItemRequestList().get(0))
                .user(getUsersList().get(0))
                .build();
        Item item2 = Item.builder()
                .id(2L)
                .description("Аккумуляторная отвертка")
                .name("Отвертка 2")
                .available(true)
                .user(getUsersList().get(1))
                .build();
        return List.of(item1, item2);
    }

    private List<ItemDtoWithBookingInfo> getItemListWithBookingInfo() {
        return getItemsList().stream()
                .map(item -> ItemMapper.toItemDtoWithBookingInfo(item, lastBookingDto, nextBookingDto))
                .collect(Collectors.toList());
    }

    private List<ItemRequest> getItemRequestList() {
        return List.of(ItemRequest.builder()
                .id(1L)
                .description("Хотелось бы воспользоваться отверткой")
                .creationDate(LocalDateTime.now())
                .user(getUsersList().get(1))
                .build());
    }

    private List<ItemRequestDtoWithItems> getItemRequestWithItemsList() {
        return List.of(ItemRequestDtoWithItems.builder()
                .id(1L)
                .description("Хотелось бы воспользоваться отверткой")
                .creationDate(START)
                .userId(getUsersList().get(1).getId())
                .items(List.of(itemDto))
                .build());
    }
}