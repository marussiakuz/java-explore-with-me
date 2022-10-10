package ru.practicum.ewm.request.personal.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.RequestNotFoundException;
import ru.practicum.ewm.error.handler.exception.UserNotFoundException;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.model.mapper.RequestMapper;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {
    @InjectMocks
    private RequestServiceImpl requestService;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    private Event event;
    private Request request;
    private User user;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(10L)
                .state(State.PUBLISHED)
                .category(Category.builder()
                        .id(5L)
                        .name("Festival")
                        .build())
                .eventDate(LocalDateTime.now().plusDays(5))
                .annotation("Amazing..")
                .description("very exciting")
                .requestModeration(false)
                .initiator(User.builder()
                        .id(9L)
                        .email("festival@gmail.com")
                        .build())
                .createdOn(LocalDateTime.now())
                .build();

        user = User.builder()
                .id(16L)
                .name("Mickael")
                .email("kosolapy@gmail.com")
                .build();

        request = Request.builder()
                .id(5L)
                .requester(user)
                .event(event)
                .build();
    }

    @Test
    void whenCreateRequestIfRequestAlreadyExistsThenThrowsConditionIsNotMetException() {
        Mockito.when(requestRepository.existsByRequesterIdAndEventId(16, 10))
                .thenReturn(true);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(16, 10));

        Assertions.assertEquals("User with id=16 already has a request to participate in event with id=10",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .existsByRequesterIdAndEventId(16, 10);

        Mockito.verify(eventRepository, Mockito.never())
                .findById(10L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(userRepository, Mockito.never())
                .findById(16L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenCreateRequestIfRequesterIsInitiatorEventThenThrowsConditionIsNotMetException() {
        Mockito.when(requestRepository.existsByRequesterIdAndEventId(16, 10))
                .thenReturn(false);

        user.setId(16L);
        event.setInitiator(user);

        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.of(event));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(16, 10));

        Assertions.assertEquals("The initiator of the event cannot create a request to participate in his " +
                        "own event",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .existsByRequesterIdAndEventId(16, 10);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(userRepository, Mockito.never())
                .findById(16L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(request);
    }

    @Test
    void whenCreateRequestIfEventNotPublishedYetThenThrowsConditionIsNotMetException() {
        Mockito.when(requestRepository.existsByRequesterIdAndEventId(16, 10))
                .thenReturn(false);

        event.setState(State.PENDING);

        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.of(event));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(16, 10));

        Assertions.assertEquals("cannot apply to participate in an unpublished event",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .existsByRequesterIdAndEventId(16, 10);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(userRepository, Mockito.never())
                .findById(16L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(request);
    }

    @Test
    void whenCreateRequestIfEventHasExhaustedLimitThenThrowsConditionIsNotMetException() {
        Mockito.when(requestRepository.existsByRequesterIdAndEventId(16, 10))
                .thenReturn(false);

        event.setParticipantLimit(10);

        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.countByEventIdAndStatus(10, Status.CONFIRMED))
                .thenReturn(10L);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(16, 10));

        Assertions.assertEquals("The event with id=10 has already reached the request limit",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .existsByRequesterIdAndEventId(16, 10);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(userRepository, Mockito.never())
                .findById(16L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(request);
    }

    @Test
    void whenCreateRequestIfEventNotExistsThenThrowsEventNotFoundException() {
        Mockito.when(requestRepository.existsByRequesterIdAndEventId(16, 10))
                .thenReturn(false);

        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> requestService.createRequest(16, 10));

        Assertions.assertEquals("Event with id=10 not found",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .existsByRequesterIdAndEventId(16, 10);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(userRepository, Mockito.never())
                .findById(16L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenCreateRequestIfUserNotExistsThenThrowsUserNotFoundException() {
        Mockito.when(requestRepository.existsByRequesterIdAndEventId(16, 10))
                .thenReturn(false);

        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.of(event));

        Mockito.when(userRepository.findById(16L))
                .thenReturn(Optional.empty());

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.createRequest(16, 10));

        Assertions.assertEquals("User with id=16 not found",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .existsByRequesterIdAndEventId(16, 10);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(16L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenCreateRequestThenCallSaveRepository() {
        Mockito.when(requestRepository.existsByRequesterIdAndEventId(16, 10))
                .thenReturn(false);

        event.setRequestModeration(true);

        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.of(event));

        Mockito.when(userRepository.findById(16L))
                .thenReturn(Optional.of(user));

        Mockito.when(requestRepository.save(Mockito.any(Request.class)))
                .thenReturn(request);

        RequestOutDto returned = requestService.createRequest(16, 10);

        assertNotNull(returned);
        assertThat(returned, equalTo(RequestMapper.toRequestOut(request)));

        Mockito.verify(requestRepository, Mockito.times(1))
                .existsByRequesterIdAndEventId(16, 10);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(16L);

        Mockito.verify(requestRepository, Mockito.times(1))
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenCancelRequestThenCallSaveRepository() {
        Mockito.when(requestRepository.findById(5L))
                .thenReturn(Optional.of(request));

        Mockito.when(requestRepository.save(request))
                .thenReturn(request);

        RequestOutDto returned = requestService.cancelRequest(16, 5);

        assertNotNull(returned);
        assertThat(returned, equalTo(RequestMapper.toRequestOut(request)));

        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(requestRepository, Mockito.times(1))
                .save(request);
    }

    @Test
    void whenCancelRequestIfRequestNotExistsThenThrowsRequestNotFoundException() {
        Mockito.when(requestRepository.findById(5L))
                .thenReturn(Optional.empty());

        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> requestService.cancelRequest(16, 5));

        Assertions.assertEquals("Request with id=5 not found",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(request);
    }

    @Test
    void whenCancelRequestIfRequesterIsAnotherThenThrowsConditionIsNotMetException() {
        Request someoneElseRequest = Request.builder()
                .requester(User.builder()
                        .id(13L)
                        .name("Initiator")
                        .email("own@ya.ru")
                        .build())
                .event(event)
                .build();

        Mockito.when(requestRepository.findById(5L))
                .thenReturn(Optional.of(someoneElseRequest));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.cancelRequest(16, 5));

        Assertions.assertEquals("the request belongs to another user",
                exception.getMessage());

        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(requestRepository, Mockito.never())
                .save(request);
    }

    @Test
    void getRequests() {
        Mockito.when(requestRepository.findAllByRequesterId(11))
                .thenReturn(List.of(request));

        List<RequestOutDto> requests = requestService.getRequests(11L);

        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0), equalTo(RequestMapper.toRequestOut(request)));

        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByRequesterId(11);
    }
}