package ru.practicum.ewm.event.personal.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.error.handler.exception.*;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.model.mapper.RequestMapper;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.util.Pagination;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class EventPersonalServiceImplTest {
    @InjectMocks
    private EventPersonalServiceImpl eventPersonalService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventStatClient eventStatClient;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    private static Category category;

    @BeforeAll
    public static void beforeAll() {
        category = Category.builder()
                .id(5L)
                .name("Cats Show")
                .build();
    }

    @Test
    void whenGetEventsThenCallFindAllByInitiatorIdRepository() {
        Event eventFirst = initEvent(2, true, State.PENDING);
        Event eventSecond = initEvent(3, false, State.CANCELED);

        Page<Event> events = new PageImpl<>(List.of(eventFirst, eventSecond));

        Mockito.when(eventRepository.findAllByInitiatorId(11, Pagination.of(0, 10)))
                .thenReturn(events);

        List<EventShortOutDto> found = eventPersonalService.getEvents(11L, 0, 10);

        assertThat(found.size(), equalTo(2));
        assertThat(found.get(0), equalTo(EventMapper.toEventShort(eventFirst, 0, 0)));
        assertThat(found.get(1), equalTo(EventMapper.toEventShort(eventSecond, 0, 0)));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAllByInitiatorId(11, Pagination.of(0, 10));
    }

    @Test
    void whenGetEventByIdIfUserIsNotInitiatorOfEventThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findByIdAndInitiatorId(2, 10))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.getEventById(10, 2));

        Assertions.assertEquals("The user with id=10 didn't initiate the event with id=2", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(2, 10);

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean());

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(2, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);
    }

    @Test
    void whenGetEventByIdIfRequestModerationTrueThenCallAddConfirmedRequestsAndViews() {
        Event event = initEvent(2, true, State.PENDING);

        Mockito.when(eventRepository.findByIdAndInitiatorId(2, 11))
                .thenReturn(Optional.of(event));

        HashMap<Long, Long> views = new HashMap<>();
        views.put(2L, 112L);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(event), true))
                .thenReturn(views);

        Mockito.when(requestRepository.countByEventIdAndStatus(2, Status.CONFIRMED))
                .thenReturn(23L);

        EventFullOutDto eventById = eventPersonalService.getEventById(11, 2);

        assertThat(eventById.getViews(), equalTo(112L));
        assertThat(eventById.getConfirmedRequests(), equalTo(23));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(2, 11);

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(event), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventIdAndStatus(2, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);
    }

    @Test
    void whenGetEventByIdIfRequestModerationFalseThenCallCountByIdAndStatusRequestRepository() {
        Event event = initEvent(3, false, State.CANCELED);

        Mockito.when(eventRepository.findByIdAndInitiatorId(3, 11))
                .thenReturn(Optional.of(event));

        HashMap<Long, Long> views = new HashMap<>();
        views.put(3L, 225L);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(event), true))
                .thenReturn(views);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(48L);

        EventFullOutDto eventById = eventPersonalService.getEventById(11, 3);

        assertThat(eventById.getViews(), equalTo(225L));
        assertThat(eventById.getConfirmedRequests(), equalTo(48));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(3, 11);

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(event), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventId(3);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(3, Status.CONFIRMED);
    }

    @Test
    void whenUpdateEventIfCategoryTheSameThenNotCallFindCategoryAndCallSaveRepository() {
        Event old = initEvent(3, false, State.PENDING);
        EventChangedDto changed = initEventChanged(3, 3, LocalDateTime.now().plusDays(2));

        Mockito.when(eventRepository.findByIdAndInitiatorId(3, 11))
                .thenReturn(Optional.of(old));

        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenReturn(old);

        HashMap<Long, Long> views = new HashMap<>();
        views.put(3L, 25L);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(old), true))
                .thenReturn(views);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(50L);

        EventFullOutDto updated = eventPersonalService.updateEvent(11, changed);

        assertThat(updated.getViews(), equalTo(25L));
        assertThat(updated.getConfirmedRequests(), equalTo(50));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(3, 11);

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(old), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventId(3L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .save(Mockito.any(Event.class));

        Mockito.verify(categoryRepository, Mockito.never())
                .findById(3L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(3, Status.CONFIRMED);
    }

    @Test
    void whenUpdateEventIfCategoryNewThenCallFindCategoryAndCallSaveRepository() {
        Event old = initEvent(3, false, State.PENDING);
        EventChangedDto changed = initEventChanged(3, 5, LocalDateTime.now().plusDays(2));

        Mockito.when(eventRepository.findByIdAndInitiatorId(3, 11))
                .thenReturn(Optional.of(old));

        Mockito.when(categoryRepository.findById(5L))
                .thenReturn(Optional.of(category));

        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenReturn(old);

        HashMap<Long, Long> views = new HashMap<>();
        views.put(3L, 7L);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(old), true))
                .thenReturn(views);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(150L);

        EventFullOutDto updated = eventPersonalService.updateEvent(11, changed);

        assertThat(updated.getViews(), equalTo(7L));
        assertThat(updated.getConfirmedRequests(), equalTo(150));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(3, 11);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(old), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventId(3);

        Mockito.verify(eventRepository, Mockito.times(1))
                .save(Mockito.any(Event.class));

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(3, Status.CONFIRMED);
    }

    @Test
    void whenUpdateEventIfStatusPublishedThenThrows() {
        Event old = initEvent(2, true, State.PUBLISHED);
        EventChangedDto changed = initEventChanged(2, 5, LocalDateTime.now().plusHours(10));

        Mockito.when(eventRepository.findByIdAndInitiatorId(2, 11))
                .thenReturn(Optional.of(old));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventPersonalService.updateEvent(11, changed));

        Assertions.assertEquals("Only pending or canceled events can be changed", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(2, 11);

        Mockito.verify(categoryRepository, Mockito.never())
                .findById(5L);

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(List.of(old), true);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(2, Status.CONFIRMED);
    }

    @Test
    void whenUpdateEventIfNewCategoryNotExistsThenThrows() {
        Event old = initEvent(2, true, State.PENDING);
        EventChangedDto changed = initEventChanged(2, 19, LocalDateTime.now().plusHours(10));

        Mockito.when(eventRepository.findByIdAndInitiatorId(2, 11))
                .thenReturn(Optional.of(old));

        Mockito.when(categoryRepository.findById(19L))
                .thenReturn(Optional.empty());

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventPersonalService.updateEvent(11, changed));

        Assertions.assertEquals("Category with id=19 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(2, 11);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(19L);

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(List.of(old), true);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(2, Status.CONFIRMED);
    }

    @Test
    void whenCreateEventIfCategoryInvalidThenThrowsCategoryNotFoundException() {
        Mockito.when(categoryRepository.findById(23L))
                .thenReturn(Optional.empty());

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(23)
                .requestModeration(true)
                .participantLimit(20)
                .annotation("Annotation")
                .description("description")
                .location(LocationDto.builder()
                        .latitude(46.4546f)
                        .longitude(52.5483f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(4))
                .title("very interesting event")
                .build();

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventPersonalService.createEvent(11, newEvent));

        Assertions.assertEquals("Category with id=23 not found", exception.getMessage());

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(23L);

        Mockito.verify(userRepository, Mockito.never())
                .findById(11L);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenCreateEventIfUserNotExistsThenThrowsUserNotFoundException() {
        Mockito.when(categoryRepository.findById(23L))
                .thenReturn(Optional.of(category));

        Mockito.when(userRepository.findById(11L))
                .thenReturn(Optional.empty());

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(23)
                .requestModeration(true)
                .participantLimit(20)
                .annotation("Annotation")
                .description("description")
                .location(LocationDto.builder()
                        .latitude(46.4546f)
                        .longitude(52.5483f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(4))
                .title("very interesting event")
                .build();

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> eventPersonalService.createEvent(11, newEvent));

        Assertions.assertEquals("User with id=11 not found", exception.getMessage());

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(23L);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(11L);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenCreateEventThenCallSaveRepository() {
        User user = User.builder().id(11L).email("cats@gmail.com").name("Goose").build();

        Mockito.when(categoryRepository.findById(23L))
                .thenReturn(Optional.of(category));

        Mockito.when(userRepository.findById(11L))
                .thenReturn(Optional.of(user));

        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenReturn(initEvent(10, true, State.PENDING));

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(23)
                .requestModeration(true)
                .participantLimit(20)
                .annotation("Annotation")
                .description("description")
                .location(LocationDto.builder()
                        .latitude(46.4546f)
                        .longitude(52.5483f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(4))
                .title("very interesting event")
                .build();

        EventFullOutDto created = eventPersonalService.createEvent(11, newEvent);

        assertThat(created.getViews(), equalTo(0L));
        assertThat(created.getConfirmedRequests(), equalTo(0));

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(23L);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(11L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenCancelEventThenCallSaveRepository() {
        Event event = initEvent(11, true, State.PENDING);

        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.of(event));

        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenReturn(event);

        eventPersonalService.cancelEvent(17, 11);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);

        Mockito.verify(eventRepository, Mockito.times(1))
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenCancelEventIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.cancelEvent(17, 11));

        Assertions.assertEquals("The user with id=17 didn't initiate the event with id=11",
                exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CANCELED", "PUBLISHED"})
    void whenCancelEventIfStatusNotPendingThenThrowsConditionIsNotMetException(String state) {
        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.ofNullable(initEvent(11, true, State.valueOf(state))));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventPersonalService.cancelEvent(17, 11));

        Assertions.assertEquals("Only pending events can be cancelled", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenGetRequestsThenCallFindAllByEventIdRequestRepository() {
        Event event = initEvent(11, true, State.PENDING);
        Request request = Request.builder()
                .event(event)
                .requester(User.builder()
                        .id(17L)
                        .email("req@gmail.com")
                        .name("Requester")
                        .build())
                .created(LocalDateTime.now())
                .status(Status.PENDING)
                .build();

        Mockito.when(eventRepository.existsByIdAndInitiatorId(11, 17))
                .thenReturn(true);

        Mockito.when(requestRepository.findAllByEventId(11))
                .thenReturn(List.of(request));

        List<RequestOutDto> requests = eventPersonalService.getRequests(17, 11);

        assertThat(requests, equalTo(List.of(RequestMapper.toRequestOut(request))));

        Mockito.verify(eventRepository, Mockito.times(1))
                .existsByIdAndInitiatorId(11, 17);

        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByEventId(11);
    }

    @Test
    void whenGetRequestsIfUserDidntInitiatedEventThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.existsByIdAndInitiatorId(11, 17))
                .thenReturn(false);

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.getRequests(17, 11));

        Assertions.assertEquals("The user with id=17 didn't initiate the event with id=11",
                exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .existsByIdAndInitiatorId(11, 17);

        Mockito.verify(requestRepository, Mockito.never())
                .findAllByEventId(11);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 20})
    void whenConfirmRequestIfRequestIsFinalForLimitThenRejectAllRemainingRequests(int count) {
        Event event = initEvent(11, true, State.PENDING);
        Request request = initRequest(22);
        List<Request> requests = IntStream.range(0, count)
                .mapToObj(this::initRequest).collect(Collectors.toList());

        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.of(event));
        Mockito.when(requestRepository.findById(22L))
                .thenReturn(Optional.of(request));
        Mockito.when(requestRepository.countByEventIdAndStatus(11, Status.CONFIRMED))
                .thenReturn(99L);
        Mockito.when(requestRepository.save(Mockito.any(Request.class)))
                .thenReturn(request);
        Mockito.when(requestRepository.findAllByEventId(11L))
                .thenReturn(requests);

        eventPersonalService.confirmRequest(17, 11, 22);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventIdAndStatus(11, Status.CONFIRMED);
        Mockito.verify(requestRepository, Mockito.times(1))
                .save(request);
        Mockito.verify(requestRepository, Mockito.times(count + 1))
                .save(Mockito.any(Request.class));
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByEventId(11L);
    }

    @Test
    void whenConfirmRequestIfLimitZeroThenNotCallSaveRequestRepository() {
        Event eventLimitZero = initEvent(11, true, State.PENDING);
        eventLimitZero.setParticipantLimit(0);
        Request request = initRequest(22);

        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.of(eventLimitZero));
        Mockito.when(requestRepository.findById(22L))
                .thenReturn(Optional.of(request));

        eventPersonalService.confirmRequest(17, 11, 22);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.never())
                .save(request);
        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(11, Status.CONFIRMED);
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByEventId(11L);
    }

    @Test
    void whenConfirmRequestIfRequestModerationFalseThenNotCallSaveRequestRepository() {
        Event event = initEvent(11, false, State.PENDING);
        Request request = initRequest(22);

        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.of(event));
        Mockito.when(requestRepository.findById(22L))
                .thenReturn(Optional.of(request));

        eventPersonalService.confirmRequest(17, 11, 22);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(11, Status.CONFIRMED);
        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByEventId(11L);
    }

    @Test
    void whenConfirmRequestIfRequestInvalidThenThrowsRequestNotFoundException() {
        Event event = initEvent(11, false, State.PENDING);

        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.of(event));
        Mockito.when(requestRepository.findById(22L))
                .thenReturn(Optional.empty());

        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> eventPersonalService.confirmRequest(17, 11, 22));

        Assertions.assertEquals("Request with id=22 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(11, Status.CONFIRMED);
        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByEventId(11L);
    }

    @Test
    void whenConfirmRequestIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.confirmRequest(17, 11, 22));

        Assertions.assertEquals("The user with id=17 didn't initiate the event with id=11",
                exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.never())
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(11, Status.CONFIRMED);
        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByEventId(11L);
    }

    @Test
    void whenRejectRequestIfUserDidntInitiateEventThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventPersonalService.confirmRequest(17, 11, 22));

        Assertions.assertEquals("The user with id=17 didn't initiate the event with id=11",
                exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.never())
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenRejectRequestIfRequestInvalidThenThrowsRequestNotFoundException() {
        Event event = initEvent(11, true, State.PUBLISHED);

        Mockito.when(eventRepository.findByIdAndInitiatorId(11, 17))
                .thenReturn(Optional.of(event));
        Mockito.when(requestRepository.findById(22L))
                .thenReturn(Optional.empty());

        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> eventPersonalService.confirmRequest(17, 11, 22));

        Assertions.assertEquals("Request with id=22 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenRejectRequestIfLimitMoreThanZeroThenCallSaveRepository() {
        Request request = initRequest(22);

        Mockito.when(eventRepository.existsByIdAndInitiatorId(11, 17))
                .thenReturn(true);
        Mockito.when(requestRepository.findById(22L))
                .thenReturn(Optional.of(request));
        Mockito.when(requestRepository.save(request))
                .thenReturn(request);

        eventPersonalService.rejectRequest(17, 11, 22);

        Mockito.verify(eventRepository, Mockito.times(1))
                .existsByIdAndInitiatorId(11, 17);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(22L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .save(Mockito.any(Request.class));
    }

    private Event initEvent(long id, boolean hasRequestModeration, State state) {
        return Event.builder()
                .id(id)
                .participantLimit(100)
                .category(Category.builder()
                        .id(3L)
                        .name("Cats")
                        .build())
                .requestModeration(hasRequestModeration)
                .state(state)
                .eventDate(LocalDateTime.now().plusDays(30))
                .annotation("Annotation")
                .description("Description")
                .title("Title")
                .initiator(User.builder()
                        .id(11L)
                        .email("cats@gmail.com")
                        .name("Goose")
                        .build())
                .build();
    }

    private EventChangedDto initEventChanged(long eventId, long catId, LocalDateTime eventDate) {
        return EventChangedDto.builder()
                .id(eventId)
                .category(catId)
                .participantLimit(333)
                .annotation("Updated annotation")
                .description("Updated description")
                .title("Updated title")
                .eventDate(eventDate)
                .build();
    }

    private Request initRequest(long id) {
        Event event = initEvent(11L, true, State.PUBLISHED);

        return Request.builder()
                .id(id)
                .event(event)
                .requester(User.builder()
                        .id(17L)
                        .email("req@gmail.com")
                        .name("Requester")
                        .build())
                .created(LocalDateTime.now())
                .status(Status.PENDING)
                .build();
    }
}