package ru.practicum.ewm.event.admin.service;

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
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventAdminChangedDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.util.Pagination;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class EventAdminServiceImplTest {
    @InjectMocks
    private EventAdminServiceImpl eventAdminService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventStatClient eventStatClient;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private CategoryRepository categoryRepository;
    private static Event eventFirst;
    private static Event eventSecond;
    private static EventAdminChangedDto changedDto;

    @BeforeAll
    public static void beforeAll() {
        eventFirst = Event.builder()
                .id(2L)
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

        changedDto = EventAdminChangedDto.builder()
                .category(5L)
                .build();
    }

    @Test
    void getEventsIfFinalConditionEmptyThenCallFindAllWithPageableRepository() {
        Page<Event> events = new PageImpl<>(List.of(eventFirst, eventSecond));

        Mockito.when(eventRepository.findAll(Mockito.any(Pagination.class)))
                .thenReturn(events);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(eventFirst, eventSecond), true))
                .thenReturn(new HashMap<>());

        Mockito.when(requestRepository.countByEventIdAndStatus(2, Status.CONFIRMED))
                .thenReturn(2L);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(15L);

        List<EventFullOutDto> found = eventAdminService.getEvents(null, null, null,
                null, null, 0, 10);

        assertThat(found.size(), equalTo(2));
        assertThat(found.get(0), equalTo(EventMapper.toEventFull(eventFirst, 2, 0)));
        assertThat(found.get(1), equalTo(EventMapper.toEventFull(eventSecond, 15, 0)));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAll(Mockito.any(Pagination.class));

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
    void getEventsIfFinalConditionNotEmptyThenCallFindAllWithBooleanExpressionAndPageableRepository() {
        Page<Event> events = new PageImpl<>(List.of(eventFirst, eventSecond));

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class)))
                .thenReturn(events);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(eventFirst, eventSecond), true))
                .thenReturn(new HashMap<>());

        Mockito.when(requestRepository.countByEventIdAndStatus(2, Status.CONFIRMED))
                .thenReturn(2L);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(15L);

        List<EventFullOutDto> found = eventAdminService.getEvents(new int[]{1, 2}, new String[]{"PUBLISHED", "PENDING"},
                null, null, null, 0, 10);

        assertThat(found.size(), equalTo(2));
        assertThat(found.get(0), equalTo(EventMapper.toEventFull(eventFirst, 2, 0)));
        assertThat(found.get(1), equalTo(EventMapper.toEventFull(eventSecond, 15, 0)));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(Pagination.class));

        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(Pagination.class));

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
    void whenUpdateEventThenSaveRepository() {
        Mockito.when(eventRepository.findById(2L))
                .thenReturn(Optional.of(eventFirst));

        Mockito.when(eventRepository.save(eventFirst))
                .thenReturn(eventFirst);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(eventFirst), true))
                .thenReturn(new HashMap<>());

        Mockito.when(requestRepository.countByEventIdAndStatus(2, Status.CONFIRMED))
                .thenReturn(2L);

        Category newCategory = Category.builder()
                .id(5L)
                .name("Programming")
                .build();

        Mockito.when(categoryRepository.findById(5L))
                .thenReturn(Optional.of(newCategory));

        EventFullOutDto updated = eventAdminService.updateEvent(2, changedDto);

        assertNotNull(updated);
        assertThat(updated, equalTo(EventMapper.toEventFull(eventFirst, 2, 0)));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(2L);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(eventFirst), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventIdAndStatus(2, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);
    }

    @Test
    void whenUpdateEventIfCategoryNotExistsThenThrowsCategoryNotFoundException() {
        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(eventSecond));

        Mockito.when(categoryRepository.findById(5L))
                .thenReturn(Optional.empty());

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventAdminService.updateEvent(3, changedDto));

        Assertions.assertEquals("Category with id=5 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(List.of(eventSecond), true);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(3, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(3);
    }

    @Test
    void whenUpdateEventIfEventNotExistsThenEventNotFoundException() {
        Mockito.when(eventRepository.findById(2L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventAdminService.updateEvent(2, changedDto));

        Assertions.assertEquals("Event with id=2 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(2L);

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(List.of(eventFirst), true);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(2, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);
    }

    @Test
    void whenPublishEventIfEventNotExistsThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventAdminService.publishEvent(3L));

        Assertions.assertEquals("Event with id=3 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenPublishEventIfStatusNotPendingThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(createEvent(State.PUBLISHED, LocalDateTime.now().plusMinutes(61))));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventAdminService.publishEvent(3L));

        Assertions.assertEquals("the event must be in the publication waiting state", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenPublishEventIfEventDateInLessOneHourThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(createEvent(State.PENDING, LocalDateTime.now().plusMinutes(59))));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventAdminService.publishEvent(3L));

        Assertions.assertEquals("the start date of the event should be no earlier than one hour after " +
                "the moment of publication", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(eventRepository, Mockito.never())
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenPublishEventThenCallSaveRepository() {
        Event published = createEvent(State.PENDING, LocalDateTime.now().plusMinutes(61));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(published));

        Mockito.when(eventRepository.save(published))
                .thenReturn(published);

        eventAdminService.publishEvent(3L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .save(Mockito.any(Event.class));
    }

    @Test
    void whenRejectEventIfEventNotExistsThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventAdminService.rejectEvent(3L));

        Assertions.assertEquals("Event with id=3 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

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
    void whenRejectEventIfStatusNotPendingThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(createEvent(State.PUBLISHED, LocalDateTime.now().plusMinutes(61))));

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> eventAdminService.rejectEvent(3L));

        Assertions.assertEquals("the event must not be published", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

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
    void whenRejectEventThenCallSaveRepository() {
        Event published = createEvent(State.PENDING, LocalDateTime.now().plusMinutes(61));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(published));

        Mockito.when(eventRepository.save(published))
                .thenReturn(published);

        HashMap<Long, Long> views = new HashMap<>(1, 22);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(published), true))
                .thenReturn(views);

        Mockito.when(requestRepository.countByEventIdAndStatus(1, Status.CONFIRMED))
                .thenReturn(5L);

        eventAdminService.rejectEvent(3L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .save(Mockito.any(Event.class));

        Mockito.verify(eventStatClient, Mockito.times(1))
                .getStatisticOnViews(List.of(published), true);

        Mockito.verify(requestRepository, Mockito.times(1))
                .countByEventIdAndStatus(1, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(Mockito.anyLong());
    }

    private Event createEvent(State state, LocalDateTime eventDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return Event.builder()
                .id(1L)
                .paid(true)
                .state(state)
                .requestModeration(true)
                .participantLimit(20)
                .annotation("Annotation")
                .description("description")
                .locationLongitude(52.5483f)
                .locationLatitude(46.4546f)
                .createdOn(LocalDateTime.parse("2022-09-29 15:46:17", formatter))
                .eventDate(eventDate)
                .category(Category.builder().id(1L).name("Theater").build())
                .initiator(User.builder().id(5L).name("Initiator").build())
                .title("very interesting event")
                .build();
    }
}