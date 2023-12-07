package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.InvalidEmailException;
import ru.practicum.shareit.exception.InvalidUserIdException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public User createUser(UserDto userDto) {
        if (isEmailEmpty(userDto.getEmail())) {
            throw new InvalidEmailException();
        }
        return userRepository.save(UserMapper.toUser(userDto));
    }

    @Transactional
    @Override
    public User updateUser(long userId, UserDto userDto) {
        if (!isUserValid(userId)) {
            throw new InvalidUserIdException(userId);
        }
        return userRepository.save(UserMapper.toUser(userRepository.findById(userId)
                .orElseThrow(() -> new InvalidUserIdException(userId)), userDto));
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        if (isInvalidId(userId)) {
            throw new InvalidUserIdException(userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new InvalidUserIdException(userId));
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public void checkUser(long userId) {
        if (!isUserValid(userId)) {
            throw new InvalidUserIdException(userId);
        }
    }

    private boolean isEmailEmpty(String email) {
        return email == null || email.isBlank() || email.isEmpty();
    }

    private boolean isInvalidId(long id) {
        return id <= 0;
    }

    private boolean isUserExists(long id) {
        return userRepository.existsById(id);
    }

    private boolean isUserValid(long id) {
        return !isInvalidId(id) && isUserExists(id);
    }
}