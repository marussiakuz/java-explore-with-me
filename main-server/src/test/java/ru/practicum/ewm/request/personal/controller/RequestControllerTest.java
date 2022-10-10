package ru.practicum.ewm.request.personal.controller;

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
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.RequestNotFoundException;
import ru.practicum.ewm.error.handler.exception.UserNotFoundException;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.personal.service.RequestService;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private RequestController requestController;
    @MockBean
    private RequestService requestService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static RequestOutDto requestOut;

    @BeforeAll
    public static void beforeAll() {
        requestOut = RequestOutDto.builder()
                .id(5L)
                .event(1L)
                .status(Status.PENDING)
                .requester(2L)
                .created(LocalDateTime.parse("2022-09-30 12:30:00", DATE_TIME))
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addRequestStatusIsOk() throws Exception {
        Mockito
                .when(requestService.createRequest(2, 1))
                .thenReturn(requestOut);

        mockMvc.perform(post("/users/2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("event").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("created")
                        .value("2022-09-30 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("requester").value(2));
    }

    @Test
    void addRequestIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/users/-2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addRequestIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/users/2/requests?eventId=-1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addRequestIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(requestService.createRequest(2, 1))
                .thenThrow(new EventNotFoundException("Event with id=1 not found"));

        mockMvc.perform(post("/users/2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=1 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void addRequestIfThrowsUserNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(requestService.createRequest(2, 1))
                .thenThrow(new UserNotFoundException("User with id=2 not found"));

        mockMvc.perform(post("/users/2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("User with id=2 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void addRequestIfDuplicateThenStatusIsConflict() throws Exception {
        Mockito
                .when(requestService.createRequest(2, 1))
                .thenThrow(new ConditionIsNotMetException("User with id=2 already has a request to participate in event " +
                        "with id=1"));

        mockMvc.perform(post("/users/2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("User with id=2 already has a request to participate in event with id=1"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void addRequestIfRequesterIsInitiatorOfTheEventThenStatusIsConflict() throws Exception {
        Mockito
                .when(requestService.createRequest(2, 1))
                .thenThrow(new ConditionIsNotMetException("The initiator of the event cannot create a request to " +
                        "participate in his own event"));

        mockMvc.perform(post("/users/2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The initiator of the event cannot create a request to participate in his " +
                                "own event"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void addRequestIfEventNotPublishedYetThenStatusIsConflict() throws Exception {
        Mockito
                .when(requestService.createRequest(2, 1))
                .thenThrow(new ConditionIsNotMetException("cannot apply to participate in an unpublished event"));

        mockMvc.perform(post("/users/2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("cannot apply to participate in an unpublished event"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void addRequestIfEventHasExhaustedLimitThenStatusIsConflict() throws Exception {
        Mockito
                .when(requestService.createRequest(2, 1))
                .thenThrow(new ConditionIsNotMetException("The event with id=1 has already reached the request limit"));

        mockMvc.perform(post("/users/2/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The event with id=1 has already reached the request limit"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void cancelRequestStatusIsOk() throws Exception {
        Mockito
                .when(requestService.cancelRequest(2, 7))
                .thenReturn(requestOut);

        mockMvc.perform(patch("/users/2/requests/7/cancel")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("event").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("created")
                        .value("2022-09-30 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("requester").value(2));
    }

    @Test
    void cancelRequestIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/-2/requests/7/cancel")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void cancelRequestIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/2/requests/-7/cancel")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void cancelRequestIfThrowsRequestNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(requestService.cancelRequest(2, 7))
                .thenThrow(new RequestNotFoundException("Request with id=7 not found"));

        mockMvc.perform(patch("/users/2/requests/7/cancel")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RequestNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Request with id=7 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void cancelRequestIfItBelongsToAnotherUserThenStatusIsConflict() throws Exception {
        Mockito
                .when(requestService.cancelRequest(2, 7))
                .thenThrow(new ConditionIsNotMetException("the request belongs to another user"));

        mockMvc.perform(patch("/users/2/requests/7/cancel")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("the request belongs to another user"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void getRequestsStatusIsOk() throws Exception {
        Mockito
                .when(requestService.getRequests(2))
                .thenReturn(List.of(requestOut));

        mockMvc.perform(get("/users/2/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].event").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].created")
                        .value("2022-09-30 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].requester").value(2));

    }

    @Test
    void getRequestsIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/users/-2/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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