package ru.practicum.ewm.event.shared.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.NoAccessRightsException;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.util.Pagination;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @InjectMocks
    private EventServiceImpl eventService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventStatClient eventStatClient;
    @Mock
    private RequestRepository requestRepository;
    private static Event eventFirst;
    private static Event eventSecond;
    private static Event notPublished;

    @BeforeAll
    public static void beforeAll() {
        eventFirst = Event.builder()
                .id(2L)
                .state(State.PUBLISHED)
                .category(Category.builder()
                        .id(2L)
                        .name("Cats")
                        .build())
                .eventDate(LocalDateTime.now().plusDays(7))
                .annotation("annotation")
                .description("description")
                .requestModeration(true)
                .initiator(User.builder()
                        .id(4L)
                        .email("user@gmail.com")
                        .build())
                .createdOn(LocalDateTime.now())
                .build();

        eventSecond = Event.builder()
                .id(3L)
                .state(State.PUBLISHED)
                .category(Category.builder()
                        .id(3L)
                        .name("Dogs")
                        .build())
                .eventDate(LocalDateTime.now().plusDays(5))
                .annotation("Another annotation")
                .description("Another description")
                .requestModeration(false)
                .initiator(User.builder()
                        .id(4L)
                        .email("dogs@gmail.com")
                        .build())
                .createdOn(LocalDateTime.now())
                .build();

        notPublished = Event.builder()
                .id(10L)
                .state(State.PENDING)
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
    }

    @Test
    void whenGetEventsIfSortByEventDateThenCallFindAllWithBooleanExpressionAndPageableRepository() {
        Page<Event> events = new PageImpl<>(List.of(eventFirst, eventSecond));

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class)))
                .thenReturn(events);

        HashMap<Long, Long> views = new HashMap<>();
        views.put(2L, 13L);
        views.put(3L, 50L);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(eventFirst, eventSecond), true))
                .thenReturn(views);

        Mockito.when(requestRepository.countByEventIdAndStatus(2, Status.CONFIRMED))
                .thenReturn(2L);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(15L);

        List<EventShortOutDto> found = eventService.getEvents(null, null, null,
                null, null, false, "EVENT_DATE", 0, 10);

        assertThat(found.size(), equalTo(2));
        assertThat(found.get(0), equalTo(EventMapper.toEventShort(eventFirst, 2, 13)));
        assertThat(found.get(1), equalTo(EventMapper.toEventShort(eventSecond, 15, 50)));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class));

        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(BooleanExpression.class));

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(eventFirst, eventSecond), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventIdAndStatus(2, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventId(3);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(3, Status.CONFIRMED);
    }

    @Test
    void whenGetEventsIfSortByViewsThenCallFindAllWithBooleanExpressionRepository() {
        Page<Event> events = new PageImpl<>(List.of(eventFirst, eventSecond));

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class)))
                .thenReturn(events);

        HashMap<Long, Long> views = new HashMap<>();
        views.put(2L, 13L);
        views.put(3L, 50L);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(eventFirst, eventSecond), true))
                .thenReturn(views);

        Mockito.when(requestRepository.countByEventIdAndStatus(2, Status.CONFIRMED))
                .thenReturn(2L);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(15L);

        List<EventShortOutDto> found = eventService.getEvents(null, null, null,
                null, null, false, "VIEWS", 0, 10);

        assertThat(found.size(), equalTo(2));
        assertThat(found.get(0), equalTo(EventMapper.toEventShort(eventFirst, 2, 13)));
        assertThat(found.get(1), equalTo(EventMapper.toEventShort(eventSecond, 15, 50)));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAll(Mockito.any(BooleanExpression.class));

        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class));

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(eventFirst, eventSecond), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventIdAndStatus(2, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventId(3);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(3, Status.CONFIRMED);
    }

    @Test
    void whenGetEventByIdIfEventNotExistsThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventService.getEventById(10));

        Assertions.assertEquals("Event with id=10 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean());

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(Mockito.anyLong());
    }

    @Test
    void whenGetEventByIdIfItNotPublishedYetThenThrowsNoAccessRightsException() {
        Mockito.when(eventRepository.findById(10L))
                .thenReturn(Optional.of(notPublished));

        final NoAccessRightsException exception = Assertions.assertThrows(
                NoAccessRightsException.class,
                () -> eventService.getEventById(10));

        Assertions.assertEquals("There are no rights to view the event with id=10 because it has not " +
                "been published yet", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(10L);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean());

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(Mockito.anyLong());
    }

    @Test
    void whenGetEventByIdThenCallFindByIdRepository() {
        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(eventSecond));

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(eventSecond), true))
                .thenReturn(new HashMap<>());

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(15L);

        EventFullOutDto found = eventService.getEventById(3);

        assertNotNull(found);
        assertThat(found, equalTo(EventMapper.toEventFull(eventSecond, 15, 0)));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(eventSecond), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventId(Mockito.anyLong());

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(Mockito.anyLong(), Mockito.any(Status.class));
    }
}