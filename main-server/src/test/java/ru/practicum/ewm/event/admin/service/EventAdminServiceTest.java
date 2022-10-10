package ru.practicum.ewm.event.admin.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.event.personal.service.EventPersonalService;
import ru.practicum.ewm.user.admin.service.UserService;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
class EventAdminServiceTest {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EntityManager em;
    private final EventAdminService eventAdminService;
    private final EventPersonalService eventPersonalService;
    private final CategoryAdminService categoryAdminService;
    @MockBean
    private final EventStatClient eventStatClient;
    private final UserService userService;
    private int[] catIds;
    private int[] userIds;

    @BeforeEach
    void setUp() {
        Mockito.when(eventStatClient.getStatisticOnViews(Mockito.anyList(), Mockito.anyBoolean()))
                .thenReturn(new HashMap<>());
        catIds = initCategories();
        userIds = initUsers();
    }

    @ParameterizedTest
    @CsvSource({"0, PUBLISHED, 2", "1, PENDING, 1", "2, CANCELED, 0"})
    void getEventsWithoutDate(int indexUser, String state, int indexCatId) {
        List<EventFullOutDto> randomEvents = initRandomEvents(100);

        List<EventOutDto> found = eventAdminService.getEvents(new int[] {userIds[indexUser]}, new String[] {state},
                new int[] {catIds[indexCatId]}, null, null, 0, 100);

        List<EventFullOutDto> filteredUsingStream = randomEvents.stream()
                .filter(event -> (event.getInitiator().getId() == (long) userIds[indexUser])
                        && (event.getState() == State.valueOf(state))
                        && (event.getCategory().getId() == (long) catIds[indexCatId]))
                .collect(Collectors.toList());

        assertThat(found.size(), equalTo(filteredUsingStream.size()));
        assertTrue(found.containsAll(filteredUsingStream));
    }

    @ParameterizedTest
    @EnumSource(value = State.class)
    void getEventsWithDate(State state) {
        List<EventFullOutDto> randomEvents = initRandomEvents(100);

        String start = LocalDateTime.now().plusDays(7).format(DATE_TIME_FORMATTER);
        String end = LocalDateTime.now().plusDays(20).format(DATE_TIME_FORMATTER);

        List<EventOutDto> found = eventAdminService.getEvents(null, new String[] {state.getState()},
                null, start, end, 0, 100);

        List<EventFullOutDto> filteredUsingStream = randomEvents.stream()
                .filter(event -> event.getState() == state
                        && event.getEventDate().isAfter(LocalDateTime.parse(start, DATE_TIME_FORMATTER))
                        && event.getEventDate().isBefore(LocalDateTime.parse(end, DATE_TIME_FORMATTER)))
                .collect(Collectors.toList());

        assertThat(found.size(), equalTo(filteredUsingStream.size()));
        assertTrue(found.containsAll(filteredUsingStream));
    }

    @Test
    void updateEvent() {
        EventFullOutDto old = initRandomEvents(1).get(0);

        CategoryInDto category = CategoryInDto.builder().name("new category").build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        LocalDateTime newEventTime = LocalDateTime.now().plusDays(1);

        EventAdminChangedDto changed = EventAdminChangedDto.builder()
                .title("Updated title")
                .requestModeration(true)
                .participantLimit(300)
                .paid(false)
                .description("Updated description")
                .eventDate(newEventTime)
                .annotation("Updated annotation")
                .category(newCategory.getId())
                .build();

        EventFullOutDto updated = eventAdminService.updateEvent(old.getId(), changed);

        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = query
                .setParameter("id", old.getId())
                .getSingleResult();

        assertThat(event, notNullValue());
        assertThat(updated, equalTo(EventMapper.toEventFull(event, 0, 0)));
        assertThat(event.getEventDate(), equalTo(newEventTime));
        assertThat(event.getTitle(), equalTo("Updated title"));
        assertThat(event.getDescription(), equalTo("Updated description"));
        assertThat(event.getAnnotation(), equalTo("Updated annotation"));
        assertThat(event.getCategory().getId(), equalTo(newCategory.getId()));
        assertThat(event.isRequestModeration(), equalTo(true));
        assertThat(event.isPaid(), equalTo(false));
        assertThat(event.getParticipantLimit(), equalTo(300));
    }

    @Test
    void publishEvent() {
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

        assertThat(saved.getState(), equalTo(State.PENDING));

        EventFullOutDto published = eventAdminService.publishEvent(saved.getId());

        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = query
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(event, notNullValue());
        assertThat(published.getState(), equalTo(State.PUBLISHED));
        assertThat(event.getState(), equalTo(State.PUBLISHED));
        assertThat(published, equalTo(EventMapper.toEventFull(event, 0, 0)));
    }

    @Test
    void rejectEvent() {
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

        assertThat(saved.getState(), equalTo(State.PENDING));

        CommentInDto commentIn = CommentInDto.builder().text("new comment").build();
        EventCommentedDto rejected = eventAdminService.rejectEvent(saved.getId(), commentIn);

        TypedQuery<Event> eventQuery = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = eventQuery
                .setParameter("id", saved.getId())
                .getSingleResult();

        TypedQuery<Comment> commentQuery = em.createQuery("Select c from Comment c where c.event.id = :id " +
                "and c.closed = false", Comment.class);
        Comment comment = commentQuery
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(event, notNullValue());
        assertThat(rejected.getState(), equalTo(State.REJECTED));
        assertThat(event.getState(), equalTo(State.REJECTED));
        assertThat(rejected, equalTo(EventMapper.toEventCommented(event, comment)));
    }

    private int[] initCategories() {
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

        return new int[] {(int) category.getId(), (int) category1.getId(), (int) category2.getId()};
    }

    private int[] initUsers() {
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

        return new int[] {Math.toIntExact(user.getId()), Math.toIntExact(user1.getId()), Math.toIntExact(user2.getId())};
    }

    private List<EventFullOutDto> initRandomEvents(int count) {
        List<EventFullOutDto> events = new CopyOnWriteArrayList<>();

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
                    long eventId = saved.getId();
                    int status = new Random().nextInt(3);
                    if (status == 0) {
                        EventOutDto canceled = eventPersonalService.cancelEvent(userId, eventId);
                        events.add((EventFullOutDto) canceled);
                    } else if (status == 1) {
                        EventFullOutDto published = eventAdminService.publishEvent(eventId);
                        events.add(published);
                    } else events.add(saved);
                });

        return events;
    }
}