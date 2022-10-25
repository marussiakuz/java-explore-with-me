package ru.practicum.ewm.event.personal.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.error.handler.exception.*;
import ru.practicum.ewm.event.EventMapperService;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.event.repository.CommentRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.model.mapper.RequestMapper;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.util.Pagination;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventPersonalServiceImpl extends EventMapperService implements EventPersonalService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventPersonalServiceImpl(EventStatClient eventStatClient, RequestRepository requestRepository,
                                    EventRepository eventRepository, CategoryRepository categoryRepository,
                                    UserRepository userRepository, CommentRepository commentRepository) {
        super(eventStatClient, requestRepository, commentRepository);
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<EventShortOutDto> getEvents(long userId, int from, int size) {
        return addConfirmedRequestsAndViews(eventRepository.findAllByInitiatorId(userId, Pagination.of(from, size))
                .getContent(), false).stream()
                .map(eventOutDto -> (EventShortOutDto) eventOutDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventOutDto getEventById(long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException(String.format("The user with id=%s didn't initiate " +
                        "the event with id=%s", userId, eventId)));

        return mapToSuitableDtoDependingOnState(event);
    }

    @Override
    public EventOutDto updateEvent(long userId, EventChangedDto eventChangedDto) {
        Long eventId = eventChangedDto.getId();

        Event beingUpdated = eventId == null ? getEventByInitiatorId(userId)
                : eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException(String.format("The user with id=%s didn't initiate " +
                        "the event with id=%s", userId, eventId)));

        Event withChanges = checkChangesAndUpdate(eventChangedDto, beingUpdated);
        Event updated = eventRepository.save(withChanges);
        log.info("the event id={} has been successfully updated", updated.getId());

        return mapToSuitableDtoDependingOnState(updated);
    }

    @Override
    public EventFullOutDto createEvent(long userId, EventInDto eventInDto) {
        long categoryId = eventInDto.getCategory();

        Event newEvent = EventMapper.toEvent(eventInDto, categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(String.format("Category with id=%s not found",
                        categoryId))), userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", userId))));

        Event saved = eventRepository.save(newEvent);
        log.info("new event has been successfully added, id={}", newEvent.getId());

        return EventMapper.toEventFull(saved, 0, 0);
    }

    @Override
    public EventOutDto cancelEvent(long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException(String.format("The user with id=%s didn't initiate " +
                        "the event with id=%s", userId, eventId)));

        if (event.getState() == State.PUBLISHED)
            throw new ConditionIsNotMetException("Events in the PUBLISHED state cannot be canceled");

        event.setState(State.CANCELED);
        Event saved = eventRepository.save(event);
        log.info("the state of the event id={} changed to CANCELED", eventId);
        return mapToSuitableDtoDependingOnState(saved);
    }

    @Override
    public List<RequestOutDto> getRequests(long userId, long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId))
            throw new EventNotFoundException(String.format("The user with id=%s didn't initiate the event with id=%s",
                    userId, eventId));

        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toRequestOut)
                .collect(Collectors.toList());
    }

    @Override
    public RequestOutDto confirmRequest(long userId, long eventId, long reqId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException(String.format("The user with id=%s didn't initiate " +
                        "the event with id=%s", userId, eventId)));

        Request request = requestRepository.findById(reqId)
                .orElseThrow(() -> new RequestNotFoundException(String.format("Request with id=%s not found", reqId)));

        if (event.getParticipantLimit() != 0 & event.isRequestModeration()) {
            int limit = event.getParticipantLimit();
            int confirmedRequests = (int) requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
            if (limit > confirmedRequests) {
                request.setStatus(Status.CONFIRMED);
                requestRepository.save(request);
                log.info("the status of the request id={} changed to CONFIRMED", reqId);
                if (limit == ++confirmedRequests) {
                    rejectAllPendingRequests(eventId);
                    log.info("all remaining requests to event id={} in the PENDING status have been changed to " +
                            "REJECTED status", eventId);
                }
            } else throw new ConditionIsNotMetException(String.format("confirmation of the request with id=%s " +
                    "was rejected due to the exhausted limit of participants", reqId));
        }

        return RequestMapper.toRequestOut(request);
    }

    @Override
    public RequestOutDto rejectRequest(long userId, long eventId, long reqId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId))
            throw new EventNotFoundException(String.format("The user with id=%s didn't initiate the event with id=%s",
                    userId, eventId));

        Request request = requestRepository.findById(reqId)
                .orElseThrow(() -> new RequestNotFoundException(String.format("Request with id=%s not found", reqId)));

        request.setStatus(Status.REJECTED);
        requestRepository.save(request);
        log.info("status of request id={} changed to REJECTED", reqId);

        return RequestMapper.toRequestOut(request);
    }

    private Event checkChangesAndUpdate(EventChangedDto changed, Event beingUpdated) {
        State currentState = beingUpdated.getState();
        if (currentState == State.PUBLISHED)
            throw new ConditionIsNotMetException("Events in the PUBLISHED state cannot be changed");
        if (changed.getEventDate() != null) beingUpdated.setEventDate(changed.getEventDate());
        if (changed.getAnnotation() != null) beingUpdated.setAnnotation(changed.getAnnotation());
        if (changed.getDescription() != null) beingUpdated.setDescription(changed.getDescription());
        if (changed.getTitle() != null) beingUpdated.setTitle(changed.getTitle());
        if (changed.getCategory() != null && !changed.getCategory().equals(beingUpdated.getCategory().getId())) {
            long catId = changed.getCategory();
            beingUpdated.setCategory(categoryRepository.findById(catId)
                    .orElseThrow(() -> new CategoryNotFoundException(String.format("Category with id=%s not found",
                            catId))));
        }
        if (changed.getParticipantLimit() != null) beingUpdated
                .setParticipantLimit(changed.getParticipantLimit());
        if (changed.getPaid() != null) beingUpdated.setPaid(changed.getPaid());
        if (currentState == State.REJECTED || currentState == State.CANCELED)
            beingUpdated.setState(currentState == State.REJECTED
                    || commentRepository.existsByEventIdAndClosedIsFalse(beingUpdated.getId()) ?
                    State.RE_MODERATION : State.PENDING);
        log.info("initial state of the event id={}: {}, current state: {}", beingUpdated.getId(), currentState,
                beingUpdated.getState());
        return beingUpdated;
    }

    private void rejectAllPendingRequests(long eventId) {
        requestRepository.findAllByEventId(eventId).stream()
                .filter(request -> request.getStatus() == Status.PENDING)
                .forEach(request -> {
                    request.setStatus(Status.REJECTED);
                    requestRepository.save(request);
                });
    }

    private Event getEventByInitiatorId(long userId) {
        QEvent event = QEvent.event;
        BooleanExpression condition = event.initiator.id.eq(userId).and(event.state.in(State.PENDING, State.CANCELED));
        Iterable<Event> events = eventRepository.findAll(condition);
        if (!events.iterator().hasNext())
            throw new ConditionIsNotMetException("not a single event with the pending or canceled status was found");
        if (events.spliterator().getExactSizeIfKnown() > 1) {
            throw new ConditionIsNotMetException("found more than one event with the pending or canceled status");
        }
        return events.iterator().next();
    }
}
