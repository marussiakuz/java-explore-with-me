package ru.practicum.ewm.user.admin.service;

import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

public interface UserService {
    UserOutDto getUsers(int[] ids, int from, int size);
    UserOutDto createUser(UserInDto userInDto);
    void deleteUser(long userId);
}
