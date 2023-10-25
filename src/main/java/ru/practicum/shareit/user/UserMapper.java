package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

public class UserMapper {

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(long userId, UserDto userDto) {
        return User.builder()
                .id(userId)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}