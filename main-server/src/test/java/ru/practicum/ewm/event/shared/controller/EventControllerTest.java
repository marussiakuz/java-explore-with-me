package ru.practicum.ewm.event.shared.controller;

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
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.NoAccessRightsException;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.model.dto.LocationDto;
import ru.practicum.ewm.event.shared.service.EventService;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {
    private final static DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private EventController eventController;
    @MockBean
    private EventService eventService;
    @MockBean
    EventStatClient eventStatClient;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static EventShortOutDto eventShortOut;
    private static EventFullOutDto eventFullOut;

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
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(eventController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getEventsStatusIsOk() throws Exception {
        Mockito
                .when(eventService.getEvents(null, null, null, null, null,
                        true, "EVENT_DATE", 0, 10))
                .thenReturn(List.of(eventShortOut));

        mockMvc.perform(get("/events?onlyAvailable=true")
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
    void getEventsIfInvalidSortingThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/events?sort=NOT_SUPPORT")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Unsupported sorting value"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void getEventsIfFromParamIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/events?from=-10")
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
    void getEventsIfSizeIsZeroOrNegativeThenStatusIsBadRequest(int value) throws Exception {
        mockMvc.perform(get("/events?from=10&size=" + value)
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
    void getEventStatusIsOk() throws Exception {
        Mockito
                .when(eventService.getEventById(11))
                .thenReturn(eventFullOut);

        mockMvc.perform(get("/events/11")
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
        mockMvc.perform(get("/events/-11")
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
    void getEventIfThrowsEventNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .when(eventService.getEventById(11))
                .thenThrow(new EventNotFoundException("Event with id=11 not found"));

        mockMvc.perform(get("/events/11")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EventNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Event with id=11 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void getEventIfNotPublishedYetThenStatusIsForbidden() throws Exception {
        Mockito
                .when(eventService.getEventById(11))
                .thenThrow(new NoAccessRightsException("There are no rights to view the event with id=11 because it " +
                                "has not been published yet"));

        mockMvc.perform(get("/events/11")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NoAccessRightsException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("There are no rights to view the event with id=11 because it has not been " +
                                "published yet"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Insufficient access rights"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("FORBIDDEN"));
    }
}