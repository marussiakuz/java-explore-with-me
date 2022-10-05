package ru.practicum.ewm.user.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.UserNotFoundException;
import ru.practicum.ewm.user.admin.service.UserService;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;
import ru.practicum.ewm.user.model.mapper.UserMapper;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private UserController userController;
    @MockBean
    private UserService userService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static UserOutDto user;
    private static UserOutDto another;

    @BeforeAll
    public static void beforeAll() {
        user = UserOutDto.builder()
                .email("ya@yandex.ru")
                .name("Yasha")
                .build();

        another = UserOutDto.builder()
                .email("googly@gmail.com")
                .name("Googler")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getUsersStatusIsOk() throws Exception {
        Mockito
                .when(userService.getUsers(null, 0, 10))
                .thenReturn(List.of(user, another));

        mockMvc.perform(get("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Yasha"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("ya@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Googler"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("googly@gmail.com"));
    }

    @Test
    void getUsersIfFromParamIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/admin/users?from=-1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("must be greater than or equal to 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -100 })
    void getUsersIfSizeIsZeroOrNegativeThenStatusIsBadRequest(int value) throws Exception {
        mockMvc.perform(get("/admin/users?from=0&size=" + value)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("must be greater than or equal to 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addUserStatusIsOk() throws Exception {
        UserInDto userIn = UserInDto.builder()
                .email("kosolapy@ya.ru")
                .name("Mikhael")
                .build();

        UserOutDto returned = UserMapper.toUserOut(UserMapper.toUser(userIn));
        returned.setId(4L);

        Mockito
                .when(userService.createUser(userIn))
                .thenReturn(returned);

        mockMvc.perform(post("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userIn)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("kosolapy@ya.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Mikhael"));
    }

    @Test
    void addUserIfNameIsBlankThenStatusIsBadRequest() throws Exception {
        UserInDto userIn = UserInDto.builder()
                .email("kosolapy@ya.ru")
                .name("       ")
                .build();

        mockMvc.perform(post("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userIn)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Name must not be blank"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addUserIfInvalidEmailThenStatusIsBadRequest() throws Exception {
        UserInDto userIn = UserInDto.builder()
                .email("kosolapy_ya.ru")
                .name("Mikhael")
                .build();

        UserOutDto returned = UserMapper.toUserOut(UserMapper.toUser(userIn));
        returned.setId(4L);

        Mockito
                .when(userService.createUser(userIn))
                .thenReturn(returned);

        mockMvc.perform(post("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userIn)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The email is incorrect"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void deleteUserStatusIsOk() throws Exception {
        mockMvc.perform(delete("/admin/users/5"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUserIfThrowsUserNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new UserNotFoundException("User with id=5 not found"))
                .when(userService).deleteUser(5);

        mockMvc.perform(delete("/admin/users/5"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("User with id=5 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void deleteUserIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/users/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }
}