package ru.practicum.ewm.event.shared.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.NoAccessRightsException;
import ru.practicum.ewm.event.admin.service.EventAdminService;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventInDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.model.dto.LocationDto;
import ru.practicum.ewm.event.model.mapper.EventMapper;
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
class EventServiceTest {
    private final EntityManager em;
    private final EventPersonalService eventPersonalService;
    private final EventService eventService;
    private final EventAdminService eventAdminService;
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
    @CsvSource({"cAt, 0, true, 300", "Dog, 1, false, 250", "sHoW, 2, false, 100"})
    void getEventsByTextCategoriesPaidAndAvailable(String text, int indexCatIds, boolean paid, int count) {
        List<EventFullOutDto> randomEvents = initRandomEvents(count);

        List<EventShortOutDto> foundByParams = eventService.getEvents(text, new int[] {catIds[indexCatIds]}, paid,
                null, null, false, "EVENT_DATE", 0, 10);

        List<EventShortOutDto> filteredUsingStream = randomEvents.stream()
                .filter(event -> event.getState() == State.PUBLISHED)
                .filter(event -> (event.getAnnotation().toLowerCase().contains(text.toLowerCase())
                        || event.getDescription().toLowerCase().contains(text.toLowerCase()))
                        && (event.isPaid() == paid) && (event.getCategory().getId() == (long) catIds[indexCatIds]))
                .sorted((e1, e2) -> e2.getEventDate().compareTo(e1.getEventDate()))
                .limit(10)
                .map(this::toEventShort)
                .collect(Collectors.toList());

        assertThat(foundByParams.size(), equalTo(filteredUsingStream.size()));
        assertTrue(foundByParams.containsAll(filteredUsingStream));
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
        EventFullOutDto published = eventAdminService.publishEvent(saved.getId());

        EventFullOutDto found = eventService.getEventById(published.getId());

        TypedQuery<Event> query = em.createQuery("Select e from Event e where e.id = :id", Event.class);
        Event event = query
                .setParameter("id", published.getId())
                .getSingleResult();

        assertThat(found, notNullValue());
        assertThat(found, equalTo(EventMapper.toEventFull(event, 0, 0)));
        assertThat(found, equalTo(published));
    }

    @Test
    void getEventByIdIfItNotPublishedYetThenThrowsNoAccessRightsException() {
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

        final NoAccessRightsException exception = Assertions.assertThrows(
                NoAccessRightsException.class,
                () ->  eventService.getEventById(saved.getId()));

        Assertions.assertEquals(String.format("There are no rights to view the event with id=%s because it has " +
                "not been published yet", saved.getId()), exception.getMessage());
    }

    @Test
    void getEventByIdIfItNotExistsThenThrowsEventNotFoundException() {
        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () ->  eventService.getEventById(100500));

        Assertions.assertEquals("Event with id=100500 not found", exception.getMessage());
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
        String[] wordsToAnnotation = {"Cats", "dogs", "Dogs", "cats", "Cats SHOW", "amazing", "beautiful but funny",
                "coffee show", "java"};
        String[] wordsToDescription = {"very interesting", "programming", "travelling show", "theater next ocean",
                "cats life"};

        List<EventFullOutDto> events = new CopyOnWriteArrayList<>();

        IntStream.range(0, count)
                .forEach(iteration -> {
                    EventInDto event = EventInDto.builder()
                            .paid(new Random().nextBoolean())
                            .category(catIds[new Random().nextInt(catIds.length)])
                            .requestModeration(new Random().nextBoolean())
                            .participantLimit(new Random().nextInt(100))
                            .annotation(wordsToAnnotation[new Random().nextInt(wordsToAnnotation.length)] + iteration)
                            .description(wordsToDescription[new Random().nextInt(wordsToDescription.length)] + iteration)
                            .location(LocationDto.builder()
                                    .latitude(46.4546f)
                                    .longitude(52.5483f)
                                    .build())
                            .eventDate(LocalDateTime.now().plusDays(iteration + 1))
                            .title("very interesting event №" + iteration)
                            .build();
                    long userId = userIds[new Random().nextInt(userIds.length)];
                    EventFullOutDto saved = eventPersonalService.createEvent(userId, event);
                    if (new Random().nextBoolean()) saved = eventAdminService.publishEvent(saved.getId());
                    events.add(saved);
                });

        return events;
    }

    private EventShortOutDto toEventShort(EventFullOutDto event) {
        return EventShortOutDto.builder()
                .id(event.getId())
                .initiator(event.getInitiator())
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .paid(event.isPaid())
                .category(event.getCategory())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .title(event.getTitle())
                .build();
    }
}