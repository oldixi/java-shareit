package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
public interface UserService {
    User cteateUser(UserDto userDto);

    User updateUser(long userId, UserDto userDto);

    void deleteUser(long userId);

    User getUserById(long userId);

    List<User> getUsers();

    void checkUser(long userId);
}