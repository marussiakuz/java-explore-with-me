package ru.practicum.ewm.compilation.admin.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.category.shared.service.CategoryService;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventInDto;
import ru.practicum.ewm.event.model.dto.LocationDto;
import ru.practicum.ewm.event.personal.service.EventPersonalService;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.admin.service.UserService;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CompilationAdminServiceTest {
    private final EntityManager em;
    private final CompilationAdminService compilationAdminService;
    private final RequestRepository requestRepository;
    @MockBean
    private final EventStatClient eventStatClient;
    private final CategoryAdminService categoryAdminService;
    private final UserService userService;
    private final EventPersonalService eventPersonalService;
    private final CategoryService categoryService;

    @Test
    void createCompilation() {
        CompilationInDto compilationIn = createCompilationInDto(true);
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());

        CompilationOutDto created = compilationAdminService.createCompilation(compilationIn);

        TypedQuery<Compilation> query = em.createQuery("Select c from Compilation c where c.id = :id",
                Compilation.class);
        Compilation compilation = query
                .setParameter("id", created.getId())
                .getSingleResult();

        assertThat(compilation, notNullValue());
        assertThat(created.getId(), notNullValue());
    }

    @Test
    void deleteCompilation() {
        CompilationInDto compilationIn = createCompilationInDto(true);
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());

        CompilationOutDto created = compilationAdminService.createCompilation(compilationIn);

        compilationAdminService.deleteCompilation(created.getId());

        Compilation compilation = em.find(Compilation.class, created.getId());
        Assertions.assertNull(compilation);
    }

    @Test
    void deleteEvent() {
        CompilationInDto compilationIn = createCompilationInDto(true);
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());

        CompilationOutDto created = compilationAdminService.createCompilation(compilationIn);
        long compId = created.getId();
        long eventId = created.getEvents().get(0).getId();
        int initialSize = created.getEvents().size();

        assertThat(created.getEvents().size(), equalTo(3));

        compilationAdminService.deleteEvent(compId, eventId);

        TypedQuery<Compilation> query = em.createQuery("Select c from Compilation c where c.id = :id",
                Compilation.class);
        Compilation compilation = query
                .setParameter("id", created.getId())
                .getSingleResult();

        assertThat(compilation.getEvents().size(), equalTo(initialSize - 1));
    }

    @Test
    void addEvent() {
        CompilationInDto compilationIn = createCompilationInDto(true);
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());

        CompilationOutDto created = compilationAdminService.createCompilation(compilationIn);
        long compId = created.getId();
        int initialSize = created.getEvents().size();

        compilationAdminService.addEvent(compId, createEventAndGetId());

        TypedQuery<Compilation> query = em.createQuery("Select c from Compilation c where c.id = :id",
                Compilation.class);
        Compilation compilation = query
                .setParameter("id", created.getId())
                .getSingleResult();

        assertThat(compilation.getEvents().size(), equalTo(initialSize + 1));
    }

    @Test
    void unpinCompilation() {
        CompilationInDto compilationIn = createCompilationInDto(true);
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());

        CompilationOutDto saved = compilationAdminService.createCompilation(compilationIn);

        compilationAdminService.unpinCompilation(saved.getId());

        TypedQuery<Compilation> query = em.createQuery("Select c from Compilation c where c.id = :savedId",
                Compilation.class);
        Compilation compilation = query
                .setParameter("savedId", saved.getId())
                .getSingleResult();

        assertThat(compilation.isPinned(), equalTo(false));
    }

    @Test
    void pinCompilation() {
        CompilationInDto compilationIn = createCompilationInDto(false);
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());

        CompilationOutDto saved = compilationAdminService.createCompilation(compilationIn);

        compilationAdminService.pinCompilation(saved.getId());

        TypedQuery<Compilation> query = em.createQuery("Select c from Compilation c where c.id = :savedId",
                Compilation.class);
        Compilation compilation = query
                .setParameter("savedId", saved.getId())
                .getSingleResult();

        assertThat(compilation.isPinned(), equalTo(true));
    }

    private CompilationInDto createCompilationInDto(boolean pinned) {
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

        return CompilationInDto.builder()
                .events(new long[] {event.getId(), event1.getId(), event2.getId()})
                .title("very amazing show")
                .pinned(pinned)
                .build();
    }

    private long createEventAndGetId() {
        UserOutDto user = userService.getUsers(null, 0, 10).get(0);
        CategoryOutDto category = categoryService.getCategories(0, 10).get(0);

        EventInDto anotherEvent = EventInDto.builder()
                .paid(false)
                .annotation("another annotation")
                .eventDate(LocalDateTime.now().plusDays(5))
                .category(category.getId())
                .participantLimit(10)
                .requestModeration(true)
                .description("Another description")
                .location(LocationDto.builder().latitude(324.2344f).longitude(23.23443f).build())
                .title("Another title")
                .build();

        return eventPersonalService.createEvent(user.getId(), anotherEvent).getId();
    }
}