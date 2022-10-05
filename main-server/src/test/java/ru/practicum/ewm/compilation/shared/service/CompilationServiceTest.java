package ru.practicum.ewm.compilation.shared.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.compilation.admin.service.CompilationAdminService;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.compilation.model.mapper.CompilationMapper;
import ru.practicum.ewm.error.handler.exception.CompilationNotFoundException;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventInDto;
import ru.practicum.ewm.event.model.dto.LocationDto;
import ru.practicum.ewm.event.personal.service.EventPersonalService;
import ru.practicum.ewm.user.admin.service.UserService;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CompilationServiceTest {
    private final EntityManager em;
    private final CompilationService compilationService;
    private final CompilationAdminService compilationAdminService;
    @MockBean
    private final EventStatClient eventStatClient;
    private final CategoryAdminService categoryAdminService;
    private final UserService userService;
    private final EventPersonalService eventPersonalService;
    private long[] eventIds;

    @BeforeEach
    void setUp() {
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());
        eventIds = createEventsAndGetIds();
    }

    @Test
    void getCompilations() {
        CompilationInDto pinned = createCompilationInDto(eventIds, true);
        CompilationInDto pinned1 = createCompilationInDto(eventIds, true);
        CompilationInDto pinned2 = createCompilationInDto(eventIds, false);
        CompilationInDto pinned3 = createCompilationInDto(eventIds, false);
        CompilationInDto pinned4 = createCompilationInDto(eventIds, false);

        CompilationOutDto compilation = compilationAdminService.createCompilation(pinned);
        CompilationOutDto compilation1 = compilationAdminService.createCompilation(pinned1);
        CompilationOutDto compilation2 = compilationAdminService.createCompilation(pinned2);
        CompilationOutDto compilation3 = compilationAdminService.createCompilation(pinned3);
        CompilationOutDto compilation4 = compilationAdminService.createCompilation(pinned4);

        List<CompilationOutDto> pinnedCompilations = compilationService.getCompilations(true, 0, 10);
        List<CompilationOutDto> notPinnedCompilations = compilationService.getCompilations(false, 0, 10);

        assertThat(pinnedCompilations.size(), equalTo(2));
        assertThat(pinnedCompilations.get(0), equalTo(compilation));
        assertThat(pinnedCompilations.get(1), equalTo(compilation1));
        assertThat(notPinnedCompilations.size(), equalTo(3));
        assertThat(notPinnedCompilations.get(0), equalTo(compilation2));
        assertThat(notPinnedCompilations.get(1), equalTo(compilation3));
        assertThat(notPinnedCompilations.get(2), equalTo(compilation4));
    }

    @Test
    void getCompilationByIdFound() {
        CompilationInDto pinned = createCompilationInDto(eventIds, true);
        CompilationOutDto saved = compilationAdminService.createCompilation(pinned);

        CompilationOutDto found = compilationService.getCompilationById(saved.getId());

        TypedQuery<Compilation> query = em.createQuery("Select c from Compilation c where c.id = :id",
                Compilation.class);
        Compilation compilation = query
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(found, notNullValue());
        assertThat(found.getTitle(), equalTo(compilation.getTitle()));
        assertThat(found.isPinned(), equalTo(compilation.isPinned()));
        assertThat(found.getEvents().size(), equalTo(compilation.getEvents().size()));
        assertThat(found, equalTo(CompilationMapper.toCompilationOut(compilation, found.getEvents())));
    }

    @Test
    void getCompilationByIdNotFound() {
        final CompilationNotFoundException exception = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.getCompilationById(100));

        Assertions.assertEquals("Compilation with id=100 not found", exception.getMessage());
    }

    private CompilationInDto createCompilationInDto(long[] events, boolean pinned) {
        return CompilationInDto.builder()
                .events(events)
                .title("title" + new Random().nextInt(Integer.MAX_VALUE))
                .pinned(pinned)
                .build();
    }

    private long[] createEventsAndGetIds() {
        CategoryInDto first = CategoryInDto.builder()
                .name("Cats Show")
                .build();

        CategoryInDto second = CategoryInDto.builder()
                .name("Theater")
                .build();

        CategoryInDto third = CategoryInDto.builder()
                .name("Museum")
                .build();

        UserInDto userFirst = UserInDto.builder()
                .name("Mickael")
                .email("kosolapy@gmail.com")
                .build();

        UserInDto userSecond = UserInDto.builder()
                .name("Yasha")
                .email("ya@ya.ru")
                .build();

        CategoryOutDto category = categoryAdminService.createCategory(first);
        CategoryOutDto category1 = categoryAdminService.createCategory(second);
        CategoryOutDto category2 = categoryAdminService.createCategory(third);

        UserOutDto user = userService.createUser(userFirst);
        UserOutDto user1 = userService.createUser(userSecond);

        EventInDto eventFirst = EventInDto.builder()
                .paid(true)
                .annotation("first annotation")
                .eventDate(LocalDateTime.now().plusDays(2))
                .category(category.getId())
                .participantLimit(100)
                .requestModeration(false)
                .description("description")
                .location(LocationDto.builder().latitude(324.2344f).longitude(23.23443f).build())
                .title("title")
                .build();

        EventInDto eventSecond = EventInDto.builder()
                .paid(false)
                .annotation("second annotation")
                .eventDate(LocalDateTime.now().plusDays(5))
                .category(category1.getId())
                .participantLimit(10)
                .requestModeration(true)
                .description("Second description")
                .location(LocationDto.builder().latitude(324.2344f).longitude(23.23443f).build())
                .title("Second title")
                .build();

        EventInDto eventThird = EventInDto.builder()
                .paid(false)
                .annotation("third annotation")
                .eventDate(LocalDateTime.now().plusDays(10))
                .category(category2.getId())
                .participantLimit(0)
                .requestModeration(true)
                .description("Third description")
                .location(LocationDto.builder().latitude(324.2344f).longitude(23.23443f).build())
                .title("Third title")
                .build();

        EventFullOutDto event = eventPersonalService.createEvent(user.getId(), eventFirst);
        EventFullOutDto event1 = eventPersonalService.createEvent(user.getId(), eventSecond);
        EventFullOutDto event2 = eventPersonalService.createEvent(user1.getId(), eventThird);

        return new long[] {event.getId(), event1.getId(), event2.getId()};
    }
}