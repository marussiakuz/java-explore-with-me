package ru.practicum.ewm.compilation.shared.service;

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
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.compilation.model.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.error.handler.exception.CompilationNotFoundException;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;
import ru.practicum.ewm.util.Pagination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {
    @InjectMocks
    private CompilationServiceImpl compilationService;
    @Mock
    private CompilationRepository compilationRepository;
    @Mock
    private EventStatClient eventStatClient;
    @Mock
    private RequestRepository requestRepository;
    private static Compilation compilation;
    private static EventShortOutDto eventFirstOut;
    private static EventShortOutDto eventSecondOut;
    private static Event eventFirst;
    private static Event eventSecond;

    @BeforeAll
    public static void beforeAll() {
        LocalDateTime firstEventDate = LocalDateTime.now().plusDays(7);
        LocalDateTime secondEventDate = LocalDateTime.now().plusDays(5);

        eventFirstOut = EventShortOutDto.builder()
                .id(2L)
                .category(CategoryOutDto.builder()
                        .id(2L)
                        .name("Cats")
                        .build())
                .eventDate(firstEventDate)
                .annotation("annotation")
                .initiator(UserShortOutDto.builder()
                        .id(4L)
                        .build())
                .build();

        eventSecondOut = EventShortOutDto.builder()
                .id(3L)
                .category(CategoryOutDto.builder()
                        .id(3L)
                        .name("Dogs")
                        .build())
                .eventDate(secondEventDate)
                .annotation("Another annotation")
                .initiator(UserShortOutDto.builder()
                        .id(4L)
                        .build())
                .build();

        eventFirst = Event.builder()
                .id(2L)
                .category(Category.builder()
                        .id(2L)
                        .name("Cats")
                        .build())
                .eventDate(firstEventDate)
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
                .eventDate(secondEventDate)
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
    void whenGetCompilationsIfPinnedIsNullThenCallFindAllRepository() {
        Page<Compilation> compilations = new PageImpl<>(List.of(compilation));

        Mockito.when(compilationRepository.findAll(Pagination.of(0, 10)))
                .thenReturn(compilations);

        List<CompilationOutDto> compilationOuts = compilationService.getCompilations(null, 0, 10);

        assertThat(compilationOuts.size(), equalTo(1));
        assertThat(compilationOuts.get(0), equalTo(CompilationMapper.toCompilationOut(compilation,
                List.of(eventFirstOut, eventSecondOut))));

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findAll(Pagination.of(0, 10));

        Mockito.verify(compilationRepository, Mockito.never())
                .findAllByPinnedTrue(Pagination.of(0, 10));

        Mockito.verify(compilationRepository, Mockito.never())
                .findAllByPinnedFalse(Pagination.of(0, 10));
    }

    @Test
    void whenGetCompilationsIfPinnedThenCallFindAllByPinnedTrueRepository() {
        Page<Compilation> compilations = new PageImpl<>(List.of(compilation));

        Mockito.when(compilationRepository.findAllByPinnedTrue(Pagination.of(0, 10)))
                .thenReturn(compilations);

        List<CompilationOutDto> compilationOuts = compilationService.getCompilations(true, 0, 10);

        assertThat(compilationOuts.size(), equalTo(1));
        assertThat(compilationOuts.get(0), equalTo(CompilationMapper.toCompilationOut(compilation,
                List.of(eventFirstOut, eventSecondOut))));

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findAllByPinnedTrue(Pagination.of(0, 10));

        Mockito.verify(compilationRepository, Mockito.never())
                .findAll(Pagination.of(0, 10));

        Mockito.verify(compilationRepository, Mockito.never())
                .findAllByPinnedFalse(Pagination.of(0, 10));
    }

    @Test
    void whenGetCompilationsIfNotPinnedThenCallFindAllByPinnedFalseRepository() {
        Page<Compilation> compilations = new PageImpl<>(List.of(compilation));

        Mockito.when(compilationRepository.findAllByPinnedFalse(Pagination.of(0, 10)))
                .thenReturn(compilations);

        List<CompilationOutDto> compilationOuts = compilationService.getCompilations(false, 0, 10);

        assertThat(compilationOuts.size(), equalTo(1));
        assertThat(compilationOuts.get(0), equalTo(CompilationMapper.toCompilationOut(compilation,
                List.of(eventFirstOut, eventSecondOut))));

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findAllByPinnedFalse(Pagination.of(0, 10));

        Mockito.verify(compilationRepository, Mockito.never())
                .findAll(Pagination.of(0, 10));

        Mockito.verify(compilationRepository, Mockito.never())
                .findAllByPinnedTrue(Pagination.of(0, 10));
    }

    @Test
    void whenGetCompilationByIdIfCompilationIsNotExistsThenThrows() {
        Mockito.when(compilationRepository.findById(2L))
                .thenReturn(Optional.empty());

        final CompilationNotFoundException exception = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.getCompilationById(2L));

        Assertions.assertEquals("Compilation with id=2 not found", exception.getMessage());

        Mockito.verify(compilationRepository, Mockito.times(1))
                .findById(2L);

        Mockito.verify(eventStatClient, Mockito.never())
                .getStatisticOnViews(List.of(eventFirst, eventSecond), false);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(2, Status.CONFIRMED);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(2);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventId(3);

        Mockito.verify(requestRepository, Mockito.never())
                .countByEventIdAndStatus(3, Status.CONFIRMED);
    }
}