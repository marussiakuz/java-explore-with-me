package ru.practicum.ewm.user.admin.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.ewm.error.handler.exception.UserNotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;
import ru.practicum.ewm.user.model.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.util.Pagination;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(16L)
                .name("Mickael")
                .email("kosolapy@gmail.com")
                .build();
    }

    @Test
    void whenGetUsersIfIdParamIdsIsNullThenCallFindAllWithPageableRepository() {
        Page<User> users = new PageImpl<>(List.of(user));

        Mockito.when(userRepository.findAll(Mockito.any(Pagination.class)))
                .thenReturn(users);

        List<UserOutDto> found = userService.getUsers(null, 0, 10);

        assertThat(found.size(), equalTo(1));
        assertThat(found.get(0), equalTo(UserMapper.toUserOut(user)));

        Mockito.verify(userRepository, Mockito.never())
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class));

        Mockito.verify(userRepository, Mockito.times(1))
                .findAll(Mockito.any(Pagination.class));
    }

    @Test
    void whenGetUsersIdIfParamIdsNotNullThenCallFindAllWithBooleanExpressionAndPageableRepository() {
        Page<User> users = new PageImpl<>(List.of(user));

        Mockito.when(userRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class)))
                .thenReturn(users);

        List<UserOutDto> found = userService.getUsers(new int[] {1, 2, 3}, 0, 10);

        assertThat(found.size(), equalTo(1));
        assertThat(found.get(0), equalTo(UserMapper.toUserOut(user)));

        Mockito.verify(userRepository, Mockito.times(1))
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class));

        Mockito.verify(userRepository, Mockito.never())
                .findAll(Mockito.any(Pagination.class));
    }

    @Test
    void whenCreateUserThenCallSaveRepository() {
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user);

        UserInDto userIn = UserInDto.builder().name("Yasha").email("ya@ya.ru").build();

        UserOutDto saved = userService.createUser(userIn);

        assertNotNull(saved);
        assertThat(saved, equalTo(UserMapper.toUserOut(user)));

        Mockito.verify(userRepository, Mockito.never())
                .save(user);
    }

    @Test
    void whenDeleteUserIfUserNotExistsThenThrowsUserNotFoundException() {
        Mockito.when(userRepository.existsById(11L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(11));

        Assertions.assertEquals("User with id=11 not found",
                exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(11L);

        Mockito.verify(userRepository, Mockito.never())
                .deleteById(11L);
    }

    @Test
    void whenDeleteUserThenCallDeleteByIdRepository() {
        Mockito.when(userRepository.existsById(11L))
                .thenReturn(true);

        userService.deleteUser(11);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(11L);

        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(11L);
    }
}