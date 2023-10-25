package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ExistEmailException;
import ru.practicum.shareit.exception.InvalidEmailException;
import ru.practicum.shareit.exception.InvalidUserIdException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User cteateUser(UserDto userDto) {
        if (isEmailEmpty(userDto.getEmail())) {
            throw new InvalidEmailException();
        }
        if (isEmailExists(0, userDto.getEmail())) {
            throw new ExistEmailException();
        }
        return userRepository.cteateUser(userDto);
    }

    @Override
    public User updateUser(long userId, UserDto userDto) {
        if (!isUserValid(userId)) {
            throw new InvalidUserIdException(userId);
        }
        if (isEmailExists(userId, userDto.getEmail())) {
            throw new ExistEmailException();
        }
        return userRepository.updateUser(userId, userDto);
    }

    @Override
    public void deleteUser(long userId) {
        if (isInvalidId(userId)) {
            throw new InvalidUserIdException(userId);
        }
        userRepository.deleteUser(userId);
    }

    @Override
    public User getUserById(long userId) {
        return userRepository.getUserById(userId).orElseThrow(() -> new InvalidUserIdException(userId));
    }

    @Override
    public List<User> getUsers() {
        return userRepository.getUsers();
    }

    private boolean isEmailEmpty(String email) {
        return email == null || email.isBlank() || email.isEmpty();
    }

    private boolean isEmailExists(long userId, String email) {
        return userRepository.isEmailExist(userId, email);
    }

    private boolean isInvalidId(long id) {
        return id <= 0;
    }

    private boolean isUserExists(long id) {
        return userRepository.isUserExists(id);
    }

    public boolean isUserValid(long id) {
        return !isInvalidId(id) && isUserExists(id);
    }
}