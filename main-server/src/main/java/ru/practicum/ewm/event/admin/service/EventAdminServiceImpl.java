package ru.practicum.ewm.event.admin.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.client.event.StatisticEventService;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.InvalidRequestException;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.model.dto.EventAdminChangedDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.LocationDto;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.util.Pagination;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventAdminServiceImpl extends StatisticEventService implements EventAdminService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public EventAdminServiceImpl(EventStatClient eventStatClient, RequestRepository requestRepository,
                                 EventRepository eventRepository, CategoryRepository categoryRepository) {
        super(eventStatClient, requestRepository);
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<EventFullOutDto> getEvents(int[] users, String[] states, int[] categories, String rangeStart,
                                           String rangeEnd, int from, int size) {
        State[] enumStates = states == null ? null
                : Arrays.stream(states)
                .map(this::mapToState)
                .toArray(State[]::new);

        LocalDateTime start = mapToLocalDateTime(rangeStart);
        LocalDateTime end = mapToLocalDateTime(rangeEnd);

        Optional<BooleanExpression> finalCondition = getFinalCondition(users, enumStates, categories, start, end);
        log.debug("the final condition has been formed: {}", finalCondition.isPresent() ? finalCondition.get() : "empty");
        Pageable pageable = Pagination.of(from, size);

        return addConfirmedRequestsAndViews(finalCondition
                .map(expression -> eventRepository.findAll(expression, pageable).getContent())
                .orElseGet(() -> eventRepository.findAll(pageable).getContent()), true).stream()
                .map(eventOutDto -> (EventFullOutDto) eventOutDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullOutDto updateEvent(long eventId, EventAdminChangedDto changedDto) {
        Event beingUpdated = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", eventId)));

        Event updated = updateChanges(changedDto, beingUpdated);
        Event saved = eventRepository.save(updated);
        log.info("event id={} has been successfully updated", eventId);

        return (EventFullOutDto) addConfirmedRequestsAndViews(List.of(saved), true).get(0);
    }

    @Override
    public EventFullOutDto publishEvent(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", eventId)));

        if(event.getState() != State.PENDING)
            throw new ConditionIsNotMetException("the event must be in the publication waiting state");

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))
            throw new ConditionIsNotMetException("the start date of the event should be no earlier than one hour " +
                    "after the moment of publication");

        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        Event saved = eventRepository.save(event);
        log.info("the state of event id={} changed to PUBLISHED", eventId);

        return EventMapper.toEventFull(saved, 0 , 0);
    }

    @Override
    public EventFullOutDto rejectEvent(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", eventId)));

        if(event.getState() == State.PUBLISHED)
            throw new ConditionIsNotMetException("the event must not be published");

        event.setState(State.CANCELED);
        Event saved = eventRepository.save(event);
        log.info("the state of event id={} changed to CANCELED", eventId);

        return (EventFullOutDto) addConfirmedRequestsAndViews(List.of(saved), true).get(0);
    }

    private Optional<BooleanExpression> getFinalCondition(int[] users, State[] states, int[] categories,
                                                          LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<BooleanExpression> conditions = new ArrayList<>();
        QEvent event = QEvent.event;

        if(users != null) {
            List<Long> userIds = Arrays.stream(users).mapToObj(Long::valueOf).collect(Collectors.toList());
            conditions.add(event.initiator.id.in(userIds));
        }
        if(states != null) conditions.add(event.state.in(states));
        if(categories != null) {
            List<Long> catIds = Arrays.stream(categories).mapToObj(Long::valueOf).collect(Collectors.toList());
            conditions.add(event.category.id.in(catIds));
        }
        if(rangeStart != null) conditions.add(event.eventDate.after(rangeStart));
        if(rangeEnd != null) conditions.add(event.eventDate.before(rangeEnd));

        return conditions.stream()
                .reduce(BooleanExpression::and);
    }

    private Event updateChanges(EventAdminChangedDto changedDto, Event event) {
        Long newCatId = changedDto.getCategory();
        LocationDto newLocation = changedDto.getLocation();

        if(newCatId != null && !newCatId.equals(event.getCategory().getId()))
            event.setCategory(categoryRepository.findById(newCatId)
                    .orElseThrow(() -> new CategoryNotFoundException(String.format("Category with id=%s not found",
                            newCatId))));
        if(changedDto.getAnnotation() != null) event.setAnnotation(changedDto.getAnnotation());
        if(changedDto.getDescription() != null) event.setDescription(changedDto.getDescription());
        if(changedDto.getTitle() != null) event.setTitle(changedDto.getTitle());
        if(changedDto.getEventDate() != null) event.setEventDate(changedDto.getEventDate());
        if(newLocation != null) {
            event.setLocationLatitude(newLocation.getLatitude());
            event.setLocationLongitude(newLocation.getLongitude());
        }
        if(changedDto.getPaid() != null) event.setPaid(changedDto.getPaid());
        if(changedDto.getParticipantLimit() != null) event.setParticipantLimit(changedDto.getParticipantLimit());
        if(changedDto.getRequestModeration() != null) event.setRequestModeration(changedDto.getRequestModeration());

        return event;
    }

    private LocalDateTime mapToLocalDateTime(String encoded) {
        if(encoded == null) return null;

        String decoded;
        try {
            decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("the following parameter could not be decoded: {}", encoded);
            throw new InvalidRequestException("there is a problem with decoding the date time parameter");
        }
        log.debug("decoding of the date time parameter has been successfully completed");

        return LocalDateTime.parse(decoded, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private State mapToState(String stateString) {
        try {
            return State.valueOf(stateString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(String.format("state is unsupported: %s", stateString));
        }
    }
}
