package ru.practicum.ewm.user.model.mapper;

import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

public class UserMapper {
    public static UserShortOutDto toUserShort(User user) {
        return UserShortOutDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static UserOutDto toUserOut(User user) {
        return UserOutDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserInDto user) {
        return User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
