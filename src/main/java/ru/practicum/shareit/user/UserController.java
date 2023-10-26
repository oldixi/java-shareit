package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public User createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Request for user {} creation", userDto.getEmail());
        return userService.cteateUser(userDto);
    }

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable long userId, @Valid @RequestBody UserDto userDto) {
        log.info("Request for user {} update", userId);
        return userService.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.info("Request for user {} deletion", userId);
        userService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable long userId) {
        log.info("Request for get user {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Request for get all users");
        return userService.getUsers();
    }
}