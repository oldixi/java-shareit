package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.InvalidPathVariableException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = UserDto.builder().email("user@user").name("user").build();
    private final UserDto userUpdatedDto = UserDto.builder().email("userUpdated@user").name("userUpdated").build();
    private final UserDto userDtoInvalidEmail = UserDto.builder().email("useruser").name("user").build();
    private final UserDto userDtoEmptyEmail = UserDto.builder().email("").name("user").build();

    @Test
    void createUser() throws Exception {
        when(userService.createUser(any())).thenReturn(UserMapper.toUser(1L, userDto));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void createUserInvalidEmail() throws Exception {
        when(userService.createUser(any())).thenThrow(InvalidPathVariableException.class);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoInvalidEmail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserEmptyEmail() throws Exception {
        when(userService.createUser(any())).thenThrow(InvalidPathVariableException.class);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoInvalidEmail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser() throws Exception {
        when(userService.updateUser(anyLong(), any())).thenReturn(UserMapper.toUser(1L, userUpdatedDto));

        mvc.perform(patch("/users/{userId}", 1L)
                        .content(mapper.writeValueAsString(userUpdatedDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(userUpdatedDto.getName())))
                .andExpect(jsonPath("$.email", is(userUpdatedDto.getEmail())));
    }

    @Test
    void deleteUser() throws Exception {
        userService.deleteUser(anyLong());

        mvc.perform(delete("/users/{userId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers() throws Exception {
        when(userService.getUsers()).thenReturn(getUsersList());

        mvc.perform(get("/users")
                        .content(mapper.writeValueAsString(userUpdatedDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserById() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(UserMapper.toUser(2L, userUpdatedDto));

        mvc.perform(get("/users/{userId}", 2L)
                        .content(mapper.writeValueAsString(userUpdatedDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2L), Long.class))
                .andExpect(jsonPath("$.name", is(userUpdatedDto.getName())))
                .andExpect(jsonPath("$.email", is(userUpdatedDto.getEmail())));
    }

    private List<User> getUsersList() {
        User user1 = User.builder().id(1L).email("user1@user").name("user1").build();
        User user2 = User.builder().id(2L).email("user2@user").name("user2").build();
        return List.of(user1, user2);
    }
}