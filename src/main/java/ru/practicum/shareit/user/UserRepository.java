package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    User cteateUser(UserDto userDto);

    User updateUser(long userId, UserDto userDto);

    void deleteUser(long userId);

    Optional<User> getUserById(long userId);

    List<User> getUsers();

    boolean isUserExists(long userId);

    boolean isEmailExist(long userId, String email);
}