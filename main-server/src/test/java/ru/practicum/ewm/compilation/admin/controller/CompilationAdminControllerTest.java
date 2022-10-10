package ru.practicum.ewm.compilation.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.compilation.admin.service.CompilationAdminService;
import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.CompilationNotFoundException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;
import ru.practicum.ewm.util.TextProcessing;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompilationAdminController.class)
class CompilationAdminControllerTest implements TextProcessing {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private CompilationAdminController compilationAdminController;
    @MockBean
    private CompilationAdminService compilationAdminService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static CompilationInDto compilationIn;
    private static CompilationOutDto compilationOut;

    @BeforeAll
    public static void beforeAll() {
        compilationIn = CompilationInDto.builder()
                .events(new long[]{1, 2, 3})
                .title("title")
                .pinned(true)
                .build();

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
                .standaloneSetup(compilationAdminController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addCompilationStatusIsOk() throws Exception {
        Mockito
                .when(compilationAdminService.createCompilation(compilationIn))
                .thenReturn(compilationOut);

        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(compilationIn)))
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
    void addCompilationIfTitleIsBlankThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CompilationInDto.builder()
                                .title("\n")
                                .events(new long[]{1, 2, 3})
                                .pinned(false)
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Title must not be blank"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addCompilationIfTitleIsTooLongThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CompilationInDto.builder()
                                .title(createText(513))
                                .events(new long[]{1, 2, 3})
                                .pinned(false)
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Title must be between 1 and 512 characters long"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addCompilationIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(compilationAdminService.createCompilation(compilationIn))
                .thenThrow(new EventNotFoundException("Event with id=3 not found"));

        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(compilationIn)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=3 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void deleteCompilationStatusIsOk() throws Exception {
        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCompilationIfCompIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/compilations/-1"))
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
    void deleteCompilationIfThrowsCompilationNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new CompilationNotFoundException("Compilation with id=1 not found"))
                .when(compilationAdminService).deleteCompilation(1);

        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CompilationNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Compilation with id=1 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void deleteEventStatusIsOk() throws Exception {
        mockMvc.perform(delete("/admin/compilations/1/events/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEventIfCompIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/compilations/-1/events/1"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {compId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void deleteEventIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/compilations/1/events/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {eventId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void deleteEventIfThrowsCompilationNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new CompilationNotFoundException("Compilation with id=5 not found"))
                .when(compilationAdminService).deleteEvent(5, 10);

        mockMvc.perform(delete("/admin/compilations/5/events/10"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CompilationNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Compilation with id=5 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void deleteEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new EventNotFoundException("Event with id=10 not found"))
                .when(compilationAdminService).deleteEvent(5, 10);

        mockMvc.perform(delete("/admin/compilations/5/events/10"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=10 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void addEventStatusIsOk() throws Exception {
        mockMvc.perform(patch("/admin/compilations/1/events/1"))
                .andExpect(status().isOk());
    }

    @Test
    void addEventIfCompIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/compilations/-5/events/1"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {compId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/compilations/1/events/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {eventId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfThrowsCompilationNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new CompilationNotFoundException("Compilation with id=5 not found"))
                .when(compilationAdminService).addEvent(5, 10);

        mockMvc.perform(patch("/admin/compilations/5/events/10"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CompilationNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Compilation with id=5 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void addEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new EventNotFoundException("Event with id=10 not found"))
                .when(compilationAdminService).addEvent(5, 10);

        mockMvc.perform(patch("/admin/compilations/5/events/10"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=10 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void unpinCompilationStatusIsOk() throws Exception {
        mockMvc.perform(delete("/admin/compilations/10/pin"))
                .andExpect(status().isOk());
    }

    @Test
    void unpinCompilationIfCompIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/compilations/-10/pin"))
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
    void unpinCompilationIfThrowsCompilationNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new CompilationNotFoundException("Compilation with id=10 not found"))
                .when(compilationAdminService).unpinCompilation(10);

        mockMvc.perform(delete("/admin/compilations/10/pin"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CompilationNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Compilation with id=10 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void pinCompilationStatusIsOk() throws Exception {
        mockMvc.perform(patch("/admin/compilations/10/pin"))
                .andExpect(status().isOk());
    }

    @Test
    void pinCompilationIfCompIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/compilations/-10/pin"))
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
    void pinCompilationIfThrowsCompilationNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new CompilationNotFoundException("Compilation with id=10 not found"))
                .when(compilationAdminService).pinCompilation(10);

        mockMvc.perform(patch("/admin/compilations/10/pin"))
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