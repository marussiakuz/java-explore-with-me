package ru.practicum.ewm.request.personal.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.RequestNotFoundException;
import ru.practicum.ewm.error.handler.exception.UserNotFoundException;
import ru.practicum.ewm.event.admin.service.EventAdminService;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventInDto;
import ru.practicum.ewm.event.model.dto.LocationDto;
import ru.practicum.ewm.event.personal.service.EventPersonalService;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.model.mapper.RequestMapper;
import ru.practicum.ewm.user.admin.service.UserService;
import ru.practicum.ewm.user.model.dto.UserInDto;
import ru.practicum.ewm.user.model.dto.UserOutDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestServiceTest {
    private final EntityManager em;
    private final RequestService requestService;
    private final EventPersonalService eventPersonalService;
    private final EventAdminService eventAdminService;
    private final CategoryAdminService categoryAdminService;
    private final UserService userService;

    @ParameterizedTest
    @CsvSource({"true, PENDING", "false, CONFIRMED"})
    void createRequest(boolean requestModeration, String status) {
        long eventId = initEvent(requestModeration);
        eventAdminService.publishEvent(eventId);

        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        RequestOutDto created = requestService.createRequest(requester.getId(), eventId);

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :id", Request.class);
        Request request = query
                .setParameter("id", created.getId())
                .getSingleResult();

        assertThat(request, notNullValue());
        assertThat(request.getStatus(), equalTo(Status.valueOf(status)));
        assertThat(request.getRequester().getId(), equalTo(requester.getId()));
        assertThat(request.getEvent().getId(), equalTo(eventId));
        assertThat(request.getEvent(), notNullValue());
    }

    @Test
    void createRequestIfEventNotExistsThenThrowsEventNotFoundException() {
        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        final EventNotFoundException exception = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> requestService.createRequest(requester.getId(), 101));

        Assertions.assertEquals("Event with id=101 not found", exception.getMessage());
    }

    @Test
    void createRequestIfRequestsAlreadyExistsThenThrowsConditionIsNotMetException() {
        long eventId = initEvent(true);
        eventAdminService.publishEvent(eventId);

        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        requestService.createRequest(requester.getId(), eventId);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(requester.getId(), eventId));

        Assertions.assertEquals(String.format("User with id=%s already has a request to participate in event with id=%s",
                requester.getId(), eventId), exception.getMessage());
    }

    @Test
    void createRequestIfUserNotExistsThenThrowsUserNotFoundException() {
        long eventId = initEvent(true);
        eventAdminService.publishEvent(eventId);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.createRequest(33, eventId));

        Assertions.assertEquals("User with id=33 not found", exception.getMessage());
    }

    @Test
    void createRequestIfRequesterIsInitiatorThenThrowsConditionIsNotMetException() {
        long eventId = initEvent(true);
        EventFullOutDto returned = eventAdminService.publishEvent(eventId);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(returned.getInitiator().getId(), eventId));

        Assertions.assertEquals("The initiator of the event cannot create a request to participate in his own " +
                "event", exception.getMessage());
    }

    @Test
    void createRequestIfEventNotPublishedYetThenThrowsConditionIsNotMetException() {
        long eventId = initEvent(true);

        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(requester.getId(), eventId));

        Assertions.assertEquals("cannot apply to participate in an unpublished event", exception.getMessage());
    }

    @Test
    void createRequestIfEventHasExhaustedLimitThenThrowsConditionIsNotMetException() {
        long eventId = initEvent(true);
        EventFullOutDto returned = eventAdminService.publishEvent(eventId);

        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        IntStream.range(0, 10)
                .forEach(iteration -> {
                    UserOutDto another = userService.createUser(UserInDto.builder()
                            .name("Requester №" + iteration)
                            .email(String.format("req%s@gmail.com", iteration))
                            .build());
                    RequestOutDto request = requestService.createRequest(another.getId(), eventId);
                    eventPersonalService.confirmRequest(returned.getInitiator().getId(), eventId, request.getId());
                });

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.createRequest(requester.getId(), eventId));

        Assertions.assertEquals(String.format("The event with id=%s has already reached the request limit", eventId),
                exception.getMessage());
    }

    @Test
    void cancelRequest() {
        long eventId = initEvent(true);
        eventAdminService.publishEvent(eventId);

        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        RequestOutDto created = requestService.createRequest(requester.getId(), eventId);

        RequestOutDto canceled = requestService.cancelRequest(requester.getId(), eventId);

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :id", Request.class);
        Request request = query
                .setParameter("id", created.getId())
                .getSingleResult();

        assertThat(canceled, notNullValue());
        assertThat(request.getStatus(), equalTo(Status.CANCELED));
        assertThat(canceled, equalTo(RequestMapper.toRequestOut(request)));
    }

    @Test
    void cancelRequestIfRequestsNotExistsThenRequestNotFoundException() {
        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> requestService.cancelRequest(11, 105));

        Assertions.assertEquals("Request with id=105 not found", exception.getMessage());
    }

    @Test
    void cancelRequestIfUserIsNotRequesterThenThrowsConditionIsNotMetException() {
        long eventId = initEvent(true);
        eventAdminService.publishEvent(eventId);

        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        RequestOutDto created = requestService.createRequest(requester.getId(), eventId);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> requestService.cancelRequest(11, created.getId()));

        Assertions.assertEquals("the request belongs to another user", exception.getMessage());
    }

    @Test
    void getRequests() {
        UserInDto user = UserInDto.builder()
                .name("Requester")
                .email("req@gmail.com")
                .build();
        UserOutDto requester = userService.createUser(user);

        IntStream.range(0, 10)
                .forEach(iteration -> {
                    long eventId = initEvent(true);
                    eventAdminService.publishEvent(eventId);
                    requestService.createRequest(requester.getId(), eventId);
                });

        List<RequestOutDto> requests = requestService.getRequests(requester.getId());

        assertThat(requests.size(), equalTo(10));

        requests
                .forEach(request -> assertThat(request.getRequester(), equalTo(requester.getId())));
    }

    private long initEvent(boolean requestModeration) {
        CategoryInDto category = CategoryInDto.builder()
                .name("new category №" + UUID.randomUUID())
                .build();
        CategoryOutDto newCategory = categoryAdminService.createCategory(category);

        UserInDto user = UserInDto.builder().email(String.format("funny%sCats@ya.ru",  UUID.randomUUID()))
                .name("Goose")
                .build();
        UserOutDto initiator = userService.createUser(user);

        EventInDto newEvent = EventInDto.builder()
                .paid(true)
                .category(newCategory.getId())
                .requestModeration(requestModeration)
                .participantLimit(10)
                .annotation("A grand cat show")
                .description("a lot of different cats")
                .location(LocationDto.builder()
                        .latitude(233.46546f)
                        .longitude(19.2434f)
                        .build())
                .eventDate(LocalDateTime.now().plusHours(24))
                .title("for those who love cats")
                .build();

        return eventPersonalService.createEvent(initiator.getId(), newEvent).getId();
    }
}