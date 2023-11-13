package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static List<UserDto> toUserDto(List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public static User toUser(long userId, UserDto userDto) {
        return User.builder()
                .id(userId)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static User toUser(User user, UserDto userDto) {
        userDto.setName(userDto.getName() != null ? userDto.getName() : user.getName());
        userDto.setEmail(userDto.getEmail() != null ? userDto.getEmail() : user.getEmail());
        return User.builder()
                .id(user.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}