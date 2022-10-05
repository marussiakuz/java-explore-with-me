package ru.practicum.ewm.user.admin.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;
import ru.practicum.ewm.user.model.mapper.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {
    private final EntityManager em;
    private final UserService userService;
    private static UserInDto userIn;
    private static UserInDto another;

    @BeforeAll
    public static void setUp() {
        userIn = UserInDto.builder()
                .email("cats@gmail.com")
                .name("Vasya")
                .build();

        another = UserInDto.builder()
                .email("ya@ya.ru")
                .name("Yasha")
                .build();
    }

    @Test
    void getUsers() {
        UserOutDto first = userService.createUser(userIn);
        UserOutDto second = userService.createUser(another);

        List<UserOutDto> users = userService.getUsers(null, 0, 10);

        assertThat(users.size(), equalTo(2));
        assertThat(users.get(0), equalTo(first));
        assertThat(users.get(1), equalTo(second));

        List<UserOutDto> usersWithCertainId = userService.getUsers(new int[] {Math.toIntExact(second.getId())},
                0, 10);

        assertThat(usersWithCertainId.size(), equalTo(1));
        assertThat(usersWithCertainId.get(0), equalTo(second));
    }

    @Test
    void createUser() {
        UserOutDto saved = userService.createUser(userIn);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User user = query
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(user, notNullValue());
        assertThat(saved.getId(), notNullValue());
        assertThat(saved, equalTo(UserMapper.toUserOut(user)));
    }

    @Test
    void deleteUser() {
        UserOutDto saved = userService.createUser(userIn);
        userService.deleteUser(saved.getId());
        User user = em.find(User.class, saved.getId());
        Assertions.assertNull(user);
    }
}