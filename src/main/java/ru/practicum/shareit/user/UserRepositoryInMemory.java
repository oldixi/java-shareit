package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository("userMemoryRepository")
public class UserRepositoryInMemory implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long generatedId = 0;

    private long generateId() {
        return ++generatedId;
    }

    @Override
    public User cteateUser(UserDto userDto) {
        User user = UserMapper.toUser(generateId(), userDto);
        users.put(user.getId(), user);
        log.info("User {} created", user.getId());
        return users.get(user.getId());
    }

    @Override
    public User updateUser(long userId, UserDto userDto) {
        users.replace(userId, UserMapper.toUser(users.get(userId), userDto));
        return users.get(userId);
    }

    @Override
    public void deleteUser(long userId) {
        users.remove(userId);
    }

    @Override
    public Optional<User> getUserById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean isUserExists(long userId) {
        return Optional.ofNullable(users.get(userId)).isPresent();
    }

    @Override
    public boolean isEmailExist(long userId, String email) {
        return users.values().stream()
                .filter(user -> user.getId() != userId)
                .map(User::getEmail)
                .anyMatch(usersEmail -> usersEmail.equals(email));
    }
}