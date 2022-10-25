package ru.practicum.ewm.event.personal.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.error.handler.exception.*;
import ru.practicum.ewm.event.admin.service.EventAdminService;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.model.mapper.RequestMapper;
import ru.practicum.ewm.request.personal.service.RequestService;
import ru.practicum.ewm.user.admin.service.UserService;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventPersonalServiceTest {
    private final EntityManager em;
    private final EventPersonalService eventPersonalService;
    private final EventAdminService eventAdminService;
    private final CategoryAdminService categoryAdminService;
    private final RequestService requestService;
    @MockBean
    private final EventStatClient eventStatClient;
    private final UserService userService;
    private long[] catIds;
    private long[] userIds;

    @BeforeEach
    void setUp() {
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());
        catIds = initCategories();
        userIds = initUsers();
    }

    @ParameterizedTest
    @ValueSource(ints = {20, 50, 100})
    void getEvents(int count) {
        List<EventShortOutDto> randomEvents = initRandomEvents(count);

        List<EventShortOutDto> foundByUserFirst = eventPersonalService.getEvents(userIds[0], 0, count);

        List<EventShortOutDto> filteredUsingStreamByUserFirst = randomEvents.stream()
                .filter(event -> (event.getInitiator().getId() == userIds[0]))
                .collect(Collectors.toList());

        List<EventShortOutDto> foundByUserSecond = eventPersonalService.getEvents(userIds[1], 0, count);

        List<EventShortOutDto> filteredUsingStreamByUserSecond = randomEvents.stream()
                .filter(event -> (event.getInitiator().getId() == userIds[1]))
                .collect(Collectors.toList());

        List<EventShortOutDto> foundByUserThird = eventPersonalService.getEvents(userIds[2], 0, count);

        List<EventShortOutDto> filteredUsingStreamByUserThird = randomEvents.stream()
                .filter(event -> (event.getInitiator().getId() == userIds[2]))
                .collect(Collectors.toList());

        assertThat(foundByUserFirst.size() + foundByUserSecond.size() + foundByUserThird.size(), equalTo(count));

        assertThat(foundByUserFirst.size(), equalTo(filteredUsingStreamByUserFirst.size()));
        assertTrue(foundByUserFirst.containsAll(filteredUsingStreamByUserFirst));

        assertThat(foundByUserSecond.size(), equalTo(filteredUsingStreamByUserSecond.size()));
        assertTrue(foundByUserSecond.containsAll(filteredUsingStreamByUserSecond));

        assertThat(foundByUserThird.size(), equalTo(filteredUsingStreamByUserThird.size()));
        assertTrue(foundByUserThird.containsAll(filteredUsingStreamByUserThird));
    }

    @Test
    void getEventById() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);

        EventOutDto eventById = eventPersonalService.getEventById(initiator.getId(), saved.getId());

        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = query
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(event, notNullValue());
        assertThat(eventById, equalTo(saved));
        assertThat(eventById, equalTo(EventMapper.toEventFull(event, 0, 0)));
    }

    @Test
    void getEventByIdIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.getEventById(10, 20));

        Assertions.assertEquals("The user with id=10 didn't initiate the event with id=20", exception.getMessage());
    }

    @Test
    void updateEvent() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);

        LocalDateTime newEventTime = LocalDateTime.now().plusDays(1);

        EventChangedDto changed = EventChangedDto.builder()
                .title("Updated title")
                .participantLimit(300)
                .paid(false)
                .description("Updated description")
                .eventDate(newEventTime)
                .annotation("Updated annotation")
                .build();

        EventOutDto updated = eventPersonalService.updateEvent(initiator.getId(), changed);

        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = query
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(event, notNullValue());
        assertThat(updated, equalTo(EventMapper.toEventFull(event, 0, 0)));
        assertThat(event.getEventDate(), equalTo(newEventTime));
        assertThat(event.getTitle(), equalTo("Updated title"));
        assertThat(event.getDescription(), equalTo("Updated description"));
        assertThat(event.getAnnotation(), equalTo("Updated annotation"));
        assertThat(event.isPaid(), equalTo(false));
        assertThat(event.getParticipantLimit(), equalTo(300));
    }

    @Test
    void updateEventIfUserDidntInitiatedEventThenThrowsEventNotFoundException() {
        EventChangedDto changed = EventChangedDto.builder()
                .id(11L)
                .title("Updated title")
                .participantLimit(300)
                .paid(false)
                .description("Updated description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .annotation("Updated annotation")
                .build();

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.updateEvent(100, changed));

        Assertions.assertEquals("The user with id=100 didn't initiate the event with id=11",
                exception.getMessage());
    }

    @Test
    void updateEventIfCategoryInvalidThenThrowsCategoryNotFoundException() {
        EventShortOutDto beingChanged = initRandomEvents(1).get(0);

        EventChangedDto changed = EventChangedDto.builder()
                .id(beingChanged.getId())
                .category(100500L)
                .title("Updated title")
                .participantLimit(300)
                .paid(false)
                .description("Updated description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .annotation("Updated annotation")
                .build();

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventPersonalService.updateEvent(beingChanged.getInitiator().getId(), changed));

        Assertions.assertEquals("Category with id=100500 not found", exception.getMessage());
    }

    @Test
    void updateEventIfEventAlreadyPublishedAndChangedHasNotIdThenThrowsConditionIsNotMetException() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        EventChangedDto changed = EventChangedDto.builder()
                .title("Updated title")
                .participantLimit(300)
                .paid(false)
                .description("Updated description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .annotation("Updated annotation")
                .build();

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventPersonalService.updateEvent(initiator.getId(), changed));

        Assertions.assertEquals("not a single event with the pending or canceled status was found",
                exception.getMessage());
    }

    @Test
    void updateEventIfEventAlreadyPublishedAndChangedHasIdThenThrowsConditionIsNotMetException() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        EventChangedDto changed = EventChangedDto.builder()
                .id(saved.getId())
                .title("Updated title")
                .participantLimit(300)
                .paid(false)
                .description("Updated description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .annotation("Updated annotation")
                .build();

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventPersonalService.updateEvent(initiator.getId(), changed));

        Assertions.assertEquals("Events in the PUBLISHED state cannot be changed", exception.getMessage());
    }

    @Test
    void createEvent() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);

        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = query
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(event, notNullValue());
        assertThat(saved, equalTo(EventMapper.toEventFull(event, 0, 0)));
        assertThat(event.getEventDate(), equalTo(eventTime));
        assertThat(event.getTitle(), equalTo("for those who love cats"));
        assertThat(event.getDescription(), equalTo("a lot of different cats"));
        assertThat(event.getAnnotation(), equalTo("A grand cat show"));
        assertThat(event.isPaid(), equalTo(true));
        assertThat(event.getCategory().getId(), equalTo(newCategory.getId()));
        assertThat(event.getCategory().getName(), equalTo(newCategory.getName()));
        assertThat(event.isRequestModeration(), equalTo(false));
        assertThat(event.getLocationLatitude(), equalTo(233.46546f));
        assertThat(event.getLocationLongitude(), equalTo(19.2434f));
        assertThat(event.getParticipantLimit(), equalTo(100500));
    }

    @Test
    void createEventIfCategoryInvalidThenThrowsCategoryNotFoundException() {
        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(100500)
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventPersonalService.createEvent(11, newEvent));

        Assertions.assertEquals("Category with id=100500 not found", exception.getMessage());
    }

    @Test
    void createEventIfUserInvalidThenThrowsUserNotFoundException() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> eventPersonalService.createEvent(100500, newEvent));

        Assertions.assertEquals("User with id=100500 not found", exception.getMessage());
    }

    @Test
    void cancelEvent() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);

        assertThat(saved.getState(), equalTo(State.PENDING));

        EventOutDto canceled = eventPersonalService.cancelEvent(initiator.getId(), saved.getId());

        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = query
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(event, notNullValue());
        assertThat(canceled, equalTo(EventMapper.toEventFull(event, 0, 0)));
        assertThat(event.getState(), equalTo(State.CANCELED));
    }

    @Test
    void cancelEventIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.cancelEvent(100500, 105));

        Assertions.assertEquals("The user with id=100500 didn't initiate the event with id=105",
                exception.getMessage());
    }

    @Test
    void cancelEventIfEventNotPendingThrowsConditionIsNotMetException() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventPersonalService.cancelEvent(initiator.getId(), saved.getId()));

        Assertions.assertEquals("Events in the PUBLISHED state cannot be canceled", exception.getMessage());
    }

    @Test
    void getRequests() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(false)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        List<RequestOutDto> createdRequests = initRequests(10, saved.getId());

        List<RequestOutDto> requests = eventPersonalService.getRequests(initiator.getId(), saved.getId());

        assertThat(requests.size(), equalTo(createdRequests.size()));
        assertTrue(requests.containsAll(createdRequests));
    }

    @Test
    void getRequestsIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.getRequests(100500, 105));

        Assertions.assertEquals("The user with id=100500 didn't initiate the event with id=105",
                exception.getMessage());
    }

    @Test
    void confirmRequest() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(true)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        RequestOutDto createdRequest = initRequests(1, saved.getId()).get(0);

        assertThat(createdRequest.getStatus(), equalTo(Status.PENDING));

        RequestOutDto confirmed = eventPersonalService.confirmRequest(initiator.getId(), saved.getId(),
                createdRequest.getId());

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :id", Request.class);
        Request request = query
                .setParameter("id", createdRequest.getId())
                .getSingleResult();

        assertThat(request, notNullValue());
        assertThat(confirmed, equalTo(RequestMapper.toRequestOut(request)));
        assertThat(request.getStatus(), equalTo(Status.CONFIRMED));
    }

    @Test
    void confirmRequestIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.confirmRequest(100500, 105, 1));

        Assertions.assertEquals("The user with id=100500 didn't initiate the event with id=105",
                exception.getMessage());
    }

    @Test
    void confirmRequestIfRequestsNotExistsThenRequestNotFoundException() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(true)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);

        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> eventPersonalService.confirmRequest(initiator.getId(), saved.getId(), 1));

        Assertions.assertEquals("Request with id=1 not found",
                exception.getMessage());
    }

    @Test
    void confirmRequestIfEventHasExhaustedLimitThenThrowsConditionIsNotMetException() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(true)
                .participantLimit(9)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        List<RequestOutDto> toConfirm = initRequests(10, saved.getId());
        toConfirm.subList(0, 9)
                .forEach(request -> eventPersonalService.confirmRequest(initiator.getId(), saved.getId(),
                        request.getId()));

        RequestOutDto extra = toConfirm.get(9);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventPersonalService.confirmRequest(initiator.getId(), saved.getId(), extra.getId()));

        Assertions.assertEquals(String.format("confirmation of the request with id=%s was rejected due to the " +
                "exhausted limit of participants", extra.getId()), exception.getMessage());
    }

    @Test
    void confirmRequestIfConfirmedFinalRequestThenRejectAllRemainingRequests() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(true)
                .participantLimit(3)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        List<RequestOutDto> allRequests = initRequests(10, saved.getId());

        allRequests.subList(0, 3)
                .forEach(request -> eventPersonalService.confirmRequest(initiator.getId(), saved.getId(),
                        request.getId()));

        allRequests.subList(3, 10)
                .forEach(request -> {
                    TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :id",
                            Request.class);
                    Request toCheck = query
                            .setParameter("id", request.getId())
                            .getSingleResult();

                    assertThat(toCheck, notNullValue());
                    assertThat(toCheck.getStatus(), equalTo(Status.REJECTED));
                });
    }

    @Test
    void rejectRequest() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(true)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);
        eventAdminService.publishEvent(saved.getId());

        RequestOutDto createdRequest = initRequests(1, saved.getId()).get(0);

        assertThat(createdRequest.getStatus(), equalTo(Status.PENDING));

        RequestOutDto confirmed = eventPersonalService.rejectRequest(initiator.getId(), saved.getId(),
                createdRequest.getId());

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :id", Request.class);
        Request request = query
                .setParameter("id", createdRequest.getId())
                .getSingleResult();

        assertThat(request, notNullValue());
        assertThat(confirmed, equalTo(RequestMapper.toRequestOut(request)));
        assertThat(request.getStatus(), equalTo(Status.REJECTED));
    }

    @Test
    void rejectRequestIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.rejectRequest(100500, 105, 1));

        Assertions.assertEquals("The user with id=100500 didn't initiate the event with id=105",
                exception.getMessage());
    }

    @Test
    void rejectRequestIfRequestsNotExistsThenRequestNotFoundException() {
        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email("funnyCats@ya.ru").name("Goose").build();
        UserOutDto initiator = userService.createUser(user);

        LocalDateTime eventTime = LocalDateTime.now().plusHours(24);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(true)
                .participantLimit(100500)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(eventTime)
                .title("for those who love cats")
                .build();

        EventFullOutDto saved = eventPersonalService.createEvent(initiator.getId(), newEvent);

        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> eventPersonalService.rejectRequest(initiator.getId(), saved.getId(), 1));

        Assertions.assertEquals("Request with id=1 not found",
                exception.getMessage());
    }

    private long[] initCategories() {
        CategoryInDto first = CategoryInDto.builder()
                .name("Cats Show")
                .build();

        CategoryInDto second = CategoryInDto.builder()
                .name("Theater")
                .build();

        CategoryInDto third = CategoryInDto.builder()
                .name("Museum")
                .build();

        CategoryOutDto category = categoryAdminService.createCategory(first);
        CategoryOutDto category1 = categoryAdminService.createCategory(second);
        CategoryOutDto category2 = categoryAdminService.createCategory(third);

        return new long[] {category.getId(), category1.getId(), category2.getId()};
    }

    private long[] initUsers() {
        UserInDto userFirst = UserInDto.builder()
                .name("Mickael")
                .email("kosolapy@gmail.com")
                .build();

        UserInDto userSecond = UserInDto.builder()
                .name("Yasha")
                .email("ya@ya.ru")
                .build();

        UserInDto userThird = UserInDto.builder()
                .name("Gosha")
                .email("go@gmail.com")
                .build();

        UserOutDto user = userService.createUser(userFirst);
        UserOutDto user1 = userService.createUser(userSecond);
        UserOutDto user2 = userService.createUser(userThird);

        return new long[] {user.getId(), user1.getId(), user2.getId()};
    }

    private List<EventShortOutDto> initRandomEvents(int count) {
        List<EventShortOutDto> events = new CopyOnWriteArrayList<>();

        IntStream.range(0, count)
                .forEach(iteration -> {
                    EventInDto event = EventInDto.builder()
                            .paid(new Random().nextBoolean())
                            .category(catIds[new Random().nextInt(catIds.length)])
                            .requestModeration(new Random().nextBoolean())
                            .participantLimit(new Random().nextInt(100))
                            .annotation("Annotation" + iteration)
                            .description("description" + iteration)
                            .location(LocationDto.builder()
                                    .latitude(46.4546f)
                                    .longitude(52.5483f)
                                    .build())
                            .eventDate(LocalDateTime.now().plusDays(iteration + 1))
                            .title("very interesting event" + iteration)
                            .build();
                    long userId = userIds[new Random().nextInt(userIds.length)];
                    EventFullOutDto saved = eventPersonalService.createEvent(userId, event);
                    TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
                    Event toSave = query
                            .setParameter("id", saved.getId())
                            .getSingleResult();
                    events.add(EventMapper.toEventShort(toSave, 0, 0));
                });

        return events;
    }

    private List<RequestOutDto> initRequests(int count, long eventId) {
        List<RequestOutDto> requests = new CopyOnWriteArrayList<>();

        IntStream.range(0, count)
                .forEach(iteration -> {
                    UserInDto requester = UserInDto.builder()
                            .name("Requester №" + iteration)
                            .email(String.format("ya%s@ya.ru", iteration))
                            .build();

                    UserOutDto saved = userService.createUser(requester);
                    RequestOutDto request = requestService.createRequest(saved.getId(), eventId);
                    requests.add(request);
                });
        return requests;
    }
}