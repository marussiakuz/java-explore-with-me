package ru.practicum.ewm.compilation.shared.controller;

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
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.compilation.shared.service.CompilationService;
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.CompilationNotFoundException;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompilationController.class)
class CompilationControllerTest {
    private final static DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private CompilationController compilationController;
    @MockBean
    private CompilationService compilationService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static CompilationOutDto compilationOut;

    @BeforeAll
    public static void beforeAll() {
        compilationOut = CompilationOutDto.builder()
                .id(1L)
                .events(List.of(createEventShort(1, LocalDateTime.parse("2022-11-11 12:30:00", DATE_TIME)),
                        createEventShort(2, LocalDateTime.now().plusDays(1)),
                        createEventShort(3, LocalDateTime.now().plusDays(30))))
                .title("title")
                .pinned(true)
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(compilationController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getCompilationsStatusIsOk() throws Exception {
        Mockito
                .when(compilationService.getCompilations(true, 0, 10))
                .thenReturn(List.of(compilationOut));

        mockMvc.perform(get("/compilations?pinned=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("title"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pinned").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[2].id").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].views").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].paid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].eventDate")
                        .value("2022-11-11 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].category.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].category.name")
                        .value("Theater"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].confirmedRequests")
                        .value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].initiator.id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].initiator.name")
                        .value("Initiator"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].events[0].title")
                        .value("very interesting event"));
    }

    @Test
    void getCompilationsIfFromParamIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/compilations?from=-10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The from must be greater than or equal to 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -100 })
    void getCompilationsIfSizeIsZeroOrNegativeThenStatusIsBadRequest(int value) throws Exception {
        mockMvc.perform(get("/compilations?from=10&size=" + value)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The min allowed value for the size is 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void getCompilationStatusIsOk() throws Exception {
        Mockito
                .when(compilationService.getCompilationById(10))
                .thenReturn(compilationOut);

        mockMvc.perform(get("/compilations/10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value("title"))
                .andExpect(MockMvcResultMatchers.jsonPath("pinned").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("events.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("events[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("events[2].id").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].views").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].paid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].eventDate")
                        .value("2022-11-11 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].category.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].category.name")
                        .value("Theater"))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].confirmedRequests")
                        .value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].initiator.id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].initiator.name")
                        .value("Initiator"))
                .andExpect(MockMvcResultMatchers.jsonPath("events[0].title")
                        .value("very interesting event"));
    }

    @Test
    void getCompilationIfCompIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/compilations/-10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void getCompilationIfThrowsCompilationNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(compilationService.getCompilationById(10))
                .thenThrow(new CompilationNotFoundException("Compilation with id=10 not found"));

        mockMvc.perform(get("/compilations/10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CompilationNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Compilation with id=10 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    private static EventShortOutDto createEventShort(long id, LocalDateTime eventDate) {
        return EventShortOutDto.builder()
                .id(id)
                .views(12)
                .paid(true)
                .eventDate(eventDate)
                .category(CategoryOutDto.builder()
                        .id(1L)
                        .name("Theater")
                        .build())
                .confirmedRequests(12)
                .initiator(UserShortOutDto.builder()
                        .id(5L)
                        .name("Initiator")
                        .build())
                .title("very interesting event")
                .build();
    }
}