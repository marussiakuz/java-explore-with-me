package ru.practicum.ewm.user.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.error.handler.exception.UserNotFoundException;
import ru.practicum.ewm.user.model.QUser;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;
import ru.practicum.ewm.user.model.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.util.Pagination;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserOutDto> getUsers(int[] ids, int from, int size) {
        Pageable pageable = Pagination.of(from, size);
        List<User> users;

        if(ids != null) {
            QUser user = QUser.user;
            List<Long> idsLong = Arrays.stream(ids).mapToObj(Long::valueOf).collect(Collectors.toList());
            users = userRepository.findAll(user.id.in(idsLong), pageable).getContent();
        } else users = userRepository.findAll(pageable).getContent();

        return users.stream()
                .map(UserMapper::toUserOut)
                .collect(Collectors.toList());
    }

    @Override
    public UserOutDto createUser(UserInDto userInDto) {
        User saved = userRepository.save(UserMapper.toUser(userInDto));
        log.info("new user id={} has been successfully added", saved.getId());
        return UserMapper.toUserOut(saved);
    }

    @Override
    public void deleteUser(long userId) {
        if(!userRepository.existsById(userId))
            throw new UserNotFoundException(String.format("User with id=%s not found", userId));

        userRepository.deleteById(userId);
        log.info("user id={} has been deleted", userId);
    }
}
