package ru.practicum.ewm.compilation.admin.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.error.handler.exception.CompilationNotFoundException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CompilationAdminServiceImplTest {
    @InjectMocks
    private CompilationAdminServiceImpl compilationAdminService;
    @Mock
    private CompilationRepository compilationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventStatClient eventStatClient;
    @Mock
    private RequestRepository requestRepository;
    private static CompilationInDto compilationIn;
    private static Compilation compilation;
    private static Event eventFirst;
    private static Event eventSecond;

    @BeforeAll
    public static void beforeAll() {
        compilationIn = CompilationInDto.builder()
                .events(new long[]{2, 3})
                .title("title")
                .pinned(true)
                .build();

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

        compilation = Compilation.builder()
                .id(3L)
                .events(List.of(eventFirst, eventSecond))
                .pinned(true)
                .title("title")
                .build();
    }

    @Test
    void whenCreateCompilationIfEventNotExistsThenThrowsEventNotFoundException() {
        Mockito.when(eventRepository.findById(2L))
                .thenReturn(Optional.of(eventFirst));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> compilationAdminService.createCompilation(compilationIn));

        Assertions.assertEquals("Event with id=3 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(2L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(compilationRepository, Mockito.never())
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenCreateCompilationIfEventExistsThenCallSaveRepository() {
        Mockito.when(eventRepository.findById(2L))
                .thenReturn(Optional.of(eventFirst));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(eventSecond));

        Mockito.when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenReturn(compilation);

        Mockito.when(eventStatClient.getStatisticOnViews(List.of(eventFirst, eventSecond), true))
                .thenReturn(new HashMap<>());

        Mockito.when(requestRepository.countByEventIdAndStatus(2, Status.CONFIRMED))
                .thenReturn(2L);

        Mockito.when(requestRepository.countByEventId(3))
                .thenReturn(15L);

        CompilationOutDto saved = compilationAdminService.createCompilation(compilationIn);

        assertNotNull(saved);
        assertThat(saved.getTitle(), equalTo(compilation.getTitle()));
        assertThat(saved.isPinned(), equalTo(compilation.isPinned()));
        assertThat(saved.getEvents().size(), equalTo(2));
        assertThat(saved.getEvents().get(0).getId(), equalTo(eventFirst.getId()));
        assertThat(saved.getEvents().get(0).getEventDate(), equalTo(eventFirst.getEventDate()));
        assertThat(saved.getEvents().get(0).getTitle(), equalTo(eventFirst.getTitle()));
        assertThat(saved.getEvents().get(0).getAnnotation(), equalTo(eventFirst.getAnnotation()));
        assertThat(saved.getEvents().get(0).getCategory().getId(), equalTo(eventFirst.getCategory().getId()));
        assertThat(saved.getEvents().get(0).getCategory().getName(), equalTo(eventFirst.getCategory().getName()));
        assertThat(saved.getEvents().get(0).getViews(), equalTo(0L));
        assertThat(saved.getEvents().get(0).getConfirmedRequests(), equalTo(2));
        assertThat(saved.getEvents().get(1).getId(), equalTo(eventSecond.getId()));
        assertThat(saved.getEvents().get(1).getViews(), equalTo(0L));
        assertThat(saved.getEvents().get(1).getConfirmedRequests(), equalTo(15));

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(2L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .save(Mockito.any(Compilation.class));

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
    void whenDeleteCompilationIfCompilationNotExistsThenThrowsCompilationNotFoundException() {
        Mockito.when(compilationRepository.existsById(5L))
                .thenReturn(false);

        final CompilationNotFoundException exception = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationAdminService.deleteCompilation(5L));

        Assertions.assertEquals("Compilation with id=5 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .existsById(5L);

        Mockito.verify(compilationRepository, Mockito.never())
                .deleteById(5L);
    }

    @Test
    void whenDeleteCompilationThenCallDeleteRepository() {
        Mockito.when(compilationRepository.existsById(5L))
                .thenReturn(true);

        compilationAdminService.deleteCompilation(5L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .existsById(5L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .deleteById(5L);
    }

    @Test
    void whenDeleteEventIfCompilationNotExistsThenCompilationNotFoundException() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.empty());

        final CompilationNotFoundException exception = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationAdminService.deleteEvent(5L, 3L));

        Assertions.assertEquals("Compilation with id=5 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventRepository, Mockito.never())
                .existsById(3L);

        Mockito.verify(compilationRepository, Mockito.never())
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenDeleteEventIfEventNotExistsThenCompilationNotFoundException() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.of(compilation));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> compilationAdminService.deleteEvent(5L, 3L));

        Assertions.assertEquals("Event with id=3 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(compilationRepository, Mockito.never())
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenDeleteEventThenCallSaveRepository() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.of(compilation));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(eventSecond));

        compilationAdminService.deleteEvent(5L, 3L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenAddEventIfCompilationNotExistsThenCompilationNotFoundException() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.empty());

        final CompilationNotFoundException exception = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationAdminService.addEvent(5L, 3L));

        Assertions.assertEquals("Compilation with id=5 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventRepository, Mockito.never())
                .findById(3L);

        Mockito.verify(compilationRepository, Mockito.never())
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenAddEventIfEventNotExistsThenCompilationNotFoundException() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.of(compilation));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.empty());

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> compilationAdminService.addEvent(5L, 3L));

        Assertions.assertEquals("Event with id=3 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(compilationRepository, Mockito.never())
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenAddEventThenCallSaveRepository() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.of(compilation));

        Mockito.when(eventRepository.findById(3L))
                .thenReturn(Optional.of(eventSecond));

        compilationAdminService.addEvent(5L, 3L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findById(3L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenUnpinCompilationIfCompilationNotExistsThenCompilationNotFoundException() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.empty());

        final CompilationNotFoundException exception = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationAdminService.unpinCompilation(5L));

        Assertions.assertEquals("Compilation with id=5 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(compilationRepository, Mockito.never())
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenUnpinCompilationThenCallSaveRepository() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.of(compilation));

        compilationAdminService.unpinCompilation(5L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenPinCompilationIfCompilationNotExistsThenCompilationNotFoundException() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.empty());

        final CompilationNotFoundException exception = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationAdminService.pinCompilation(5L));

        Assertions.assertEquals("Compilation with id=5 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(compilationRepository, Mockito.never())
                .save(Mockito.any(Compilation.class));
    }

    @Test
    void whenPinCompilationThenCallSaveRepository() {
        Mockito.when(compilationRepository.findById(5L))
                .thenReturn(Optional.of(compilation));

        compilationAdminService.pinCompilation(5L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(compilationRepository, Mockito.times(1))
                .save(Mockito.any(Compilation.class));
    }
}