package ru.practicum.ewm.event.admin.controller;

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
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.InvalidRequestException;
import ru.practicum.ewm.event.admin.service.EventAdminService;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.dto.CommentInDto;
import ru.practicum.ewm.event.model.dto.EventAdminChangedDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.LocationDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventAdminController.class)
class EventAdminControllerTest {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private EventAdminController eventAdminController;
    @MockBean
    private EventAdminService eventAdminService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static EventFullOutDto eventFullOut;
    private static EventAdminChangedDto eventAdminChanged;
    private static CommentInDto commentIn;

    @BeforeAll
    public static void beforeAll() {
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

        eventAdminChanged = EventAdminChangedDto.builder()
                .category(5L)
                .build();

        commentIn = CommentInDto.builder()
                .text("comment")
                .build();

    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(eventAdminController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }


    @Test
    void getEventsStatusIsOk() throws Exception {
        Mockito
                .when(eventAdminService.getEvents(null, null, null, null,
                        null, 0, 10))
                .thenReturn(List.of(eventFullOut));

        mockMvc.perform(get("/admin/events")
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
                        .value("Annotation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description")
                        .value("description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].participantLimit").value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].requestModeration").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].state").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].location.lat").value(46.4546f))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].location.lon").value(52.5483f))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdOn")
                        .value("2022-09-29 15:46:17"));
    }

    @Test
    void getEventsIfFromParamIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/admin/events?from=-10")
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
        mockMvc.perform(get("/admin/events?size=" + value)
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
    void getEventsIfInvalidStatesParamThenStatusIsBadRequest() throws Exception {
        Mockito
                .when(eventAdminService.getEvents(null, new String[]{"PUBLISHED", "NOT"}, null,
                        null, null, 0, 10))
                .thenThrow(new InvalidRequestException("state is unsupported: NOT"));

        mockMvc.perform(get("/admin/events?states=PUBLISHED&states=NOT")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("state is unsupported: NOT"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The request was made with errors"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void getEventsIfInvalidEncodedDateTimeParamThenStatusIsBadRequest() throws Exception {
        Mockito
                .when(eventAdminService.getEvents(null, null, null,
                        "20XX-19-18YY22:22:22", null, 0, 10))
                .thenThrow(new InvalidRequestException("there is a problem with decoding the date time parameter"));

        mockMvc.perform(get("/admin/events?rangeStart=20XX-19-18YY22:22:22")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("there is a problem with decoding the date time parameter"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The request was made with errors"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void updateEventStatusIsOk() throws Exception {
        Mockito
                .when(eventAdminService.updateEvent(15, eventAdminChanged))
                .thenReturn(eventFullOut);

        mockMvc.perform(put("/admin/events/15")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventAdminChanged)))
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
                .andExpect(MockMvcResultMatchers.jsonPath("location.lat").value(46.4546f))
                .andExpect(MockMvcResultMatchers.jsonPath("location.lon").value(52.5483f))
                .andExpect(MockMvcResultMatchers.jsonPath("createdOn")
                        .value("2022-09-29 15:46:17"));
    }

    @Test
    void updateEventIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(put("/admin/events/-15")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventAdminChanged)))
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
                .when(eventAdminService.updateEvent(15, eventAdminChanged))
                .thenThrow(new EventNotFoundException("Event with id=15 not found"));

        mockMvc.perform(put("/admin/events/15")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventAdminChanged)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=15 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void updateEventIfThrowsCategoryNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventAdminService.updateEvent(15, eventAdminChanged))
                .thenThrow(new CategoryNotFoundException("Category with id=5 not found"));

        mockMvc.perform(put("/admin/events/15")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(eventAdminChanged)))
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
    void publishEventStatusIsOk() throws Exception {
        mockMvc.perform(patch("/admin/events/16/publish"))
                .andExpect(status().isOk());
    }

    @Test
    void publishEventIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/events/-16/publish"))
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
    void publishEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new EventNotFoundException("Event with id=16 not found"))
                .when(eventAdminService).publishEvent(16);

        mockMvc.perform(patch("/admin/events/16/publish"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=16 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void publishEventIfEventIsAlreadyPublishedThenStatusIsConflict() throws Exception {
        Mockito
                .doThrow(new ConditionIsNotMetException("the event must be in the publication waiting state"))
                .when(eventAdminService).publishEvent(16);

        mockMvc.perform(patch("/admin/events/16/publish"))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("the event must be in the publication waiting state"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void publishEventIfEventDateIsTooEarlyThenStatusIsConflict() throws Exception {
        Mockito
                .doThrow(new ConditionIsNotMetException("the start date of the event should be no earlier than " +
                        "one hour after the moment of publication"))
                .when(eventAdminService).publishEvent(16);

        mockMvc.perform(patch("/admin/events/16/publish"))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("the start date of the event should be no earlier than one hour after " +
                                "the moment of publication"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }

    @Test
    void rejectEventStatusIsOk() throws Exception {
        mockMvc.perform(patch("/admin/events/16/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentIn)))
                .andExpect(status().isOk());
    }

    @Test
    void rejectEventIfEventIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/events/-16/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentIn)))
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
    void rejectEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new EventNotFoundException("Event with id=16 not found"))
                .when(eventAdminService).rejectEvent(16, commentIn);

        mockMvc.perform(patch("/admin/events/16/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentIn)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=16 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void rejectEventIfStatePendingThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new ConditionIsNotMetException("the event must not be published"))
                .when(eventAdminService).rejectEvent(16, commentIn);

        mockMvc.perform(patch("/admin/events/16/reject")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentIn)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("the event must not be published"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }
}