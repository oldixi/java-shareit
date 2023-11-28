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
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingInfo;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    private static final LocalDateTime START = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime END = LocalDateTime.now().minusDays(1);

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

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

    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .authorName("user")
            .text("Отличная отвертка. Спасибо.")
            .created(START)
            .build();

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenReturn(ItemMapper.toItemDtoWithRequestId(
                        ItemMapper.toItem(1L, itemDto, getUsersList().get(0), getItemRequestList().get(0))));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));
    }

    @Test
    void updateUser() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any()))
                .thenReturn(ItemMapper.toItem(1L, itemUpdatedDto, getUsersList().get(0), getItemRequestList().get(0)));

        mvc.perform(patch("/items/{itemId}", 1L)
                        .content(mapper.writeValueAsString(itemUpdatedDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(itemUpdatedDto.getName())))
                .andExpect(jsonPath("$.available", is(itemUpdatedDto.getAvailable())))
                .andExpect(jsonPath("$.description", is(itemUpdatedDto.getDescription())));
    }

    @Test
    void deleteItem() throws Exception {
        itemService.deleteItem(anyLong(), anyLong());

        mvc.perform(delete("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createComment() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any(Comment.class)))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())));
    }

    @Test
    void getItems() throws Exception {
        when(itemService.getItemsByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(getItemListWithBookingInfo());

        System.out.println(getItemListWithBookingInfo());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from","0")
                        .param("size", "3")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchItems() throws Exception {
        when(itemService.searchItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDto, item2Dto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "отверт")
                        .param("from","0")
                        .param("size", "3")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItemByIdWithCommentsAndBookingInfo(anyLong(), anyLong()))
                .thenReturn(ItemMapper.toItemDtoWithCommentsAndBookingInfo(getItemsList().get(0), comments,
                        lastBookingDto, nextBookingDto));

        mvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(getItemsList().get(0).getName())))
                .andExpect(jsonPath("$.description", is(getItemsList().get(0).getDescription())));
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
                .creationDate(START)
                .user(getUsersList().get(1))
                .build());
    }
}