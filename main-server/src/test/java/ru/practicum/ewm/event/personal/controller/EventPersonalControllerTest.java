package ru.practicum.ewm.event.personal.controller;

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
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.*;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.event.personal.service.EventPersonalService;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventPersonalController.class)
class EventPersonalControllerTest {
    private final static DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private EventPersonalController eventPersonalController;
    @MockBean
    private EventPersonalService eventPersonalService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static EventFullOutDto eventFullOut;
    private static EventShortOutDto eventShortOut;
    private static EventChangedDto eventChanged;
    private static EventInDto eventInDto;
    private static RequestOutDto requestOut;

    @BeforeAll
    public static void beforeAll() {
        eventShortOut = EventShortOutDto.builder()
                .id(1L)
                .views(12)
                .paid(true)
                .annotation("Annotation")
                .eventDate(LocalDateTime.parse("2022-11-11 12:30:00", DATE_TIME))
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

        eventFullOut = EventFullOutDto.builder()
                .id(1L)
                .views(12)
                .paid(true)
                .state(State.PENDING)
                .requestModeration(true)
                .participantLimit(20)
                .annotation("Annotation")
                .description("description")
                .location(LocationDto.builder()
                        .latitude(46.4546f)
                        .longitude(52.5483f)
                        .build())
                .createdOn(LocalDateTime.parse("2022-09-29 15:46:17", DATE_TIME))
                .eventDate(LocalDateTime.parse("2022-11-11 12:30:00", DATE_TIME))
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

        eventChanged = EventChangedDto.builder()
                .category(5L)
                .build();

        eventInDto = EventInDto.builder()
                .annotation("Annotation")
                .description("description")
                .title("title")
                .category(5)
                .location(LocationDto.builder()
                        .longitude(56.347543f)
                        .latitude(234.43545f)
                        .build())
                .requestModeration(true)
                .participantLimit(100)
                .eventDate(LocalDateTime.now().plusHours(2).plusMinutes(5))
                .paid(true)
                .build();

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
                .standaloneSetup(eventPersonalController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getEventsStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.getEvents(5, 0, 10))
                .thenReturn(List.of(eventShortOut));

        mockMvc.perform(get("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].views").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].paid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventDate")
                        .value("2022-11-11 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].category.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].category.name")
                        .value("Theater"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].confirmedRequests").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].initiator.id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].initiator.name")
                        .value("Initiator"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title")
                        .value("very interesting event"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].annotation")
                        .value("Annotation"));
    }

    @Test
    void getEventsIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/users/-5/events")
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
    void getEventsIfFromParamIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/users/5/events?from=-100")
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
    void getEventsIfSizeIsZeroOrNegativeThenStatusIsBadRequest(int value) throws Exception {
        mockMvc.perform(get("/users/5/events?size=" + value)
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
    void getEventStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.getEventById(5, 10))
                .thenReturn(eventFullOut);

        mockMvc.perform(get("/users/5/events/10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("views").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("paid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("eventDate")
                        .value("2022-11-11 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("category.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("category.name").value("Theater"))
                .andExpect(MockMvcResultMatchers.jsonPath("confirmedRequests").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.name").value("Initiator"))
                .andExpect(MockMvcResultMatchers.jsonPath("title")
                        .value("very interesting event"))
                .andExpect(MockMvcResultMatchers.jsonPath("annotation").value("Annotation"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("description"))
                .andExpect(MockMvcResultMatchers.jsonPath("participantLimit").value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("requestModeration").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("state").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("location.latitude").value(46.4546f))
                .andExpect(MockMvcResultMatchers.jsonPath("location.longitude").value(52.5483f))
                .andExpect(MockMvcResultMatchers.jsonPath("createdOn")
                        .value("2022-09-29 15:46:17"));
    }

    @Test
    void getEventIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/users/5/events/-10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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
    void getEventIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/users/-5/events/10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {userId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void getEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.getEventById(5, 10))
                .thenThrow(new EventNotFoundException("Event with id=10 not found"));

        mockMvc.perform(get("/users/5/events/10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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
    void updateEventStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.updateEvent(5, eventChanged))
                .thenReturn(eventFullOut);

        mockMvc.perform(patch("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("views").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("paid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("eventDate")
                        .value("2022-11-11 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("category.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("category.name").value("Theater"))
                .andExpect(MockMvcResultMatchers.jsonPath("confirmedRequests").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.name").value("Initiator"))
                .andExpect(MockMvcResultMatchers.jsonPath("title")
                        .value("very interesting event"))
                .andExpect(MockMvcResultMatchers.jsonPath("annotation").value("Annotation"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("description"))
                .andExpect(MockMvcResultMatchers.jsonPath("participantLimit").value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("requestModeration").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("state").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("location.latitude").value(46.4546f))
                .andExpect(MockMvcResultMatchers.jsonPath("location.longitude").value(52.5483f))
                .andExpect(MockMvcResultMatchers.jsonPath("createdOn")
                        .value("2022-09-29 15:46:17"));

        Mockito.verify(eventPersonalService, Mockito.times(1))
                .updateEvent(5, eventChanged);
    }

    @Test
    void updateEventIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/-5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
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
    void updateEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.updateEvent(5, eventChanged))
                .thenThrow(new EventNotFoundException("The user with id=5 didn't initiate the event with id=10"));

        mockMvc.perform(patch("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The user with id=5 didn't initiate the event with id=10"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void updateEventIfThrowsCategoryNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.updateEvent(5, eventChanged))
                .thenThrow(new CategoryNotFoundException("Category with id=5 not found"));

        mockMvc.perform(patch("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CategoryNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Category with id=5 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void updateEventIfEventDateTooEarlyThenStatusConflict() throws Exception {
        Mockito
                .when(eventPersonalService.updateEvent(5, eventChanged))
                .thenThrow(new ConditionIsNotMetException("The event must not take place earlier than two hours from " +
                        "the current time"));

        mockMvc.perform(patch("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The event must not take place earlier than two hours from the current " +
                                "time"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void updateEventIfStatusNotCanceledOrPendingThenStatusConflict() throws Exception {
        Mockito
                .when(eventPersonalService.updateEvent(5, eventChanged))
                .thenThrow(new ConditionIsNotMetException("Only pending or canceled events can be changed"));

        mockMvc.perform(patch("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Only pending or canceled events can be changed"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void updateEventIfNoPendingOrCanceledEventsThenStatusConflict() throws Exception {
        Mockito
                .when(eventPersonalService.updateEvent(5, eventChanged))
                .thenThrow(new ConditionIsNotMetException("not a single event with the pending or canceled status " +
                        "was found"));

        mockMvc.perform(patch("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("not a single event with the pending or canceled status was found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void updateEventIfEventIdAbsentAndMoreThanOneSuitableEventsThenStatusConflict() throws Exception {
        Mockito
                .when(eventPersonalService.updateEvent(5, eventChanged))
                .thenThrow(new ConditionIsNotMetException("found more than one event with the pending or canceled " +
                        "status"));

        mockMvc.perform(patch("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventChanged)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("found more than one event with the pending or canceled status"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void addEventStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.createEvent(Mockito.anyLong(), Mockito.any(EventInDto.class)))
                .thenReturn(eventFullOut);

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("views").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("paid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("eventDate")
                        .value("2022-11-11 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("category.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("category.name").value("Theater"))
                .andExpect(MockMvcResultMatchers.jsonPath("confirmedRequests").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.name").value("Initiator"))
                .andExpect(MockMvcResultMatchers.jsonPath("title")
                        .value("very interesting event"))
                .andExpect(MockMvcResultMatchers.jsonPath("annotation").value("Annotation"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("description"))
                .andExpect(MockMvcResultMatchers.jsonPath("participantLimit").value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("requestModeration").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("state").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("location.latitude").value(46.4546f))
                .andExpect(MockMvcResultMatchers.jsonPath("location.longitude").value(52.5483f))
                .andExpect(MockMvcResultMatchers.jsonPath("createdOn")
                        .value("2022-09-29 15:46:17"));
    }

    @Test
    void addEventIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/users/-5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventInDto)))
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
    void addEventIfAnnotationIsBlankThenStatusIsBadRequest() throws Exception {
        EventInDto added = createEventInWithNullField(null, "title", "description");

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(added)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Annotation must not be blank"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfInvalidAnnotationThenStatusIsBadRequest() throws Exception {
        EventInDto added = createEventIn(261, 100, 100,
                LocalDateTime.now().plusHours(2).plusMinutes(1));

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(added)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Annotation must be between 1 and 200 characters long"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfTitleIsBlankThenStatusIsBadRequest() throws Exception {
        EventInDto added = createEventInWithNullField("annotation", null, "description");

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(added)))
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
    void addEventIfInvalidTitleThenStatusIsBadRequest() throws Exception {
        EventInDto added = createEventIn(100, 100, 121,
                LocalDateTime.now().plusHours(2).plusMinutes(1));

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(added)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Title must be between 1 and 120 characters long"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfDescriptionIsBlankThenStatusIsBadRequest() throws Exception {
        EventInDto added = createEventInWithNullField("annotation", "title", null);

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(added)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Description must not be blank"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfInvalidDescriptionThenStatusIsBadRequest() throws Exception {
        EventInDto added = createEventIn(100, 1001, 100,
                LocalDateTime.now().plusHours(2).plusMinutes(1));

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(added)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Description must be between 1 and 1000 characters long"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfEventDateInLessThanTwoHoursThenStatusIsBadRequest() throws Exception {
        EventInDto added = createEventIn(100, 100, 100,
                LocalDateTime.now().plusHours(1).plusMinutes(59));

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(added)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The event must not take place earlier than two hours from the current time"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addEventIfThrowsUserNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.createEvent(Mockito.anyLong(), Mockito.any(EventInDto.class)))
                .thenThrow(new UserNotFoundException("User with id=5 not found"));

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventInDto)))
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
    void addEventIfThrowsCategoryNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.createEvent(Mockito.anyLong(), Mockito.any(EventInDto.class)))
                .thenThrow(new CategoryNotFoundException("Category with id=1 not found"));

        mockMvc.perform(post("/users/5/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CategoryNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Category with id=1 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void cancelEventStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.cancelEvent(5, 7))
                .thenReturn(eventFullOut);

        mockMvc.perform(patch("/users/5/events/7")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("views").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("paid").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("eventDate")
                        .value("2022-11-11 12:30:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("category.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("category.name").value("Theater"))
                .andExpect(MockMvcResultMatchers.jsonPath("confirmedRequests").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.id").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("initiator.name").value("Initiator"))
                .andExpect(MockMvcResultMatchers.jsonPath("title")
                        .value("very interesting event"))
                .andExpect(MockMvcResultMatchers.jsonPath("annotation").value("Annotation"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("description"))
                .andExpect(MockMvcResultMatchers.jsonPath("participantLimit").value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("requestModeration").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("state").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("location.latitude").value(46.4546f))
                .andExpect(MockMvcResultMatchers.jsonPath("location.longitude").value(52.5483f))
                .andExpect(MockMvcResultMatchers.jsonPath("createdOn")
                        .value("2022-09-29 15:46:17"));
    }

    @Test
    void cancelEventIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/-5/events/7")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {userId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void cancelEventIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/5/events/-7")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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
    void cancelEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.cancelEvent(5, 7))
                .thenThrow(new EventNotFoundException("Event with id=7 not found"));

        mockMvc.perform(patch("/users/5/events/7")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=7 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void cancelEventIfNotPendingThenStatusIsConflict() throws Exception {
        Mockito
                .when(eventPersonalService.cancelEvent(5, 7))
                .thenThrow(new ConditionIsNotMetException("Only pending events can be cancelled"));

        mockMvc.perform(patch("/users/5/events/7")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Only pending events can be cancelled"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void getRequestsStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.getRequests(5, 7))
                .thenReturn(List.of(requestOut));

        mockMvc.perform(get("/users/5/events/7/requests")
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
        mockMvc.perform(get("/users/-5/events/1/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {userId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void getRequestsIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/users/5/events/-1/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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
    void getRequestsIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.getRequests(5, 7))
                .thenThrow(new EventNotFoundException("Event with id=7 not found"));

        mockMvc.perform(get("/users/5/events/7/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=7 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void confirmRequestStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.confirmRequest(5, 1, 7))
                .thenReturn(requestOut);

        mockMvc.perform(patch("/users/5/events/1/requests/7/confirm")
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
    void confirmRequestIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/-5/events/1/requests/7/confirm")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {userId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void confirmRequestIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/5/events/-1/requests/7/confirm")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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
    void confirmRequestIfReqIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/5/events/1/requests/-7/confirm")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {reqId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void confirmRequestIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.confirmRequest(5, 1, 7))
                .thenThrow(new EventNotFoundException("The user with id=5 didn't initiate the event with id=1"));

        mockMvc.perform(patch("/users/5/events/1/requests/7/confirm")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The user with id=5 didn't initiate the event with id=1"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void confirmRequestIfThrowsRequestNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.confirmRequest(5, 1, 7))
                .thenThrow(new RequestNotFoundException("Request with id=7 not found"));

        mockMvc.perform(patch("/users/5/events/1/requests/7/confirm")
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
    void confirmRequestIfEventHasExhaustedLimitThenStatusIsConflict() throws Exception {
        Mockito
                .when(eventPersonalService.confirmRequest(5, 1, 7))
                .thenThrow(new ConditionIsNotMetException("confirmation of the request with id=7 was rejected due " +
                        "to the exhausted limit of participants"));

        mockMvc.perform(patch("/users/5/events/1/requests/7/confirm")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("confirmation of the request with id=7 was rejected due to the exhausted " +
                                "limit of participants"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void rejectRequestStatusIsOk() throws Exception {
        Mockito
                .when(eventPersonalService.rejectRequest(5, 1, 7))
                .thenReturn(requestOut);

        mockMvc.perform(patch("/users/5/events/1/requests/7/reject")
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
    void rejectRequestIfUserIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/-5/events/1/requests/7/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {userId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void rejectRequestIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/5/events/-1/requests/7/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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
    void rejectRequestIfReqIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/5/events/1/requests/-7/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value {reqId} must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void rejectRequestIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.rejectRequest(5, 1, 7))
                .thenThrow(new EventNotFoundException("The user with id=5 didn't initiate the event with id=1"));

        mockMvc.perform(patch("/users/5/events/1/requests/7/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The user with id=5 didn't initiate the event with id=1"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void rejectRequestIfThrowsRequestNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventPersonalService.rejectRequest(5, 1, 7))
                .thenThrow(new RequestNotFoundException("Request with id=7 not found"));

        mockMvc.perform(patch("/users/5/events/1/requests/7/reject")
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

    private EventInDto createEventInWithNullField(String annotation, String title, String description) {
        return EventInDto.builder()
                .annotation(annotation)
                .description(description)
                .title(title)
                .category(5)
                .location(LocationDto.builder()
                        .longitude(56.347543f)
                        .latitude(234.43545f)
                        .build())
                .requestModeration(true)
                .participantLimit(100)
                .eventDate(LocalDateTime.now().plusHours(2).plusMinutes(1))
                .paid(true)
                .build();
    }

    private EventInDto createEventIn(int annotationLength, int descriptionLength, int titleLength,
                                     LocalDateTime eventDate) {
        return EventInDto.builder()
                .annotation(createText(annotationLength))
                .description(createText(descriptionLength))
                .title(createText(titleLength))
                .category(5)
                .location(LocationDto.builder()
                        .longitude(56.347543f)
                        .latitude(234.43545f)
                        .build())
                .requestModeration(true)
                .participantLimit(100)
                .eventDate(eventDate)
                .paid(true)
                .build();
    }

    private String createText(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        IntStream.range(0, length).forEach(iteration -> {
                char letter = (char) random.nextInt(Character.MAX_VALUE + 1);
                builder.append(letter);
        });

        return builder.toString();
    }
}