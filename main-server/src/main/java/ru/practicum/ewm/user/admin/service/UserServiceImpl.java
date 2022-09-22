package ru.practicum.ewm.user.admin.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserOutDto getUsers(int[] ids, int from, int size) {
        return null;
    }

    @Override
    public UserOutDto createUser(UserInDto userInDto) {
        return null;
    }

    @Override
    public void deleteUser(long userId) {

    }
}
