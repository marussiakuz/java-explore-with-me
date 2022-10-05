package ru.practicum.ewm.event.shared.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.client.event.StatisticEventService;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.NoAccessRightsException;
import ru.practicum.ewm.event.enums.SortingEvents;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.model.QRequest;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.util.Pagination;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventServiceImpl extends StatisticEventService implements EventService {
    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventStatClient eventStatClient, RequestRepository requestRepository, EventRepository eventRepository) {
        super(eventStatClient, requestRepository);
        this.eventRepository = eventRepository;
    }

    @Override
    public List<EventShortOutDto> getEvents(String text, int[] categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, boolean onlyAvailable, String sort,
                                            int from, int size) {
        BooleanExpression finalCondition = getFinalCondition(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable);
        log.debug("the final condition has been successfully formed: {}", finalCondition);

        if(SortingEvents.valueOf(sort) == SortingEvents.EVENT_DATE) return getEventsSortedByDate(finalCondition, from,
                size);

        return getEventsSortedByViews(finalCondition, from, size);
    }

    @Override
    public EventFullOutDto getEventById(long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", id)));

        if (event.getState() != State.PUBLISHED)
            throw new NoAccessRightsException(String.format("There are no rights to view the event with id=%s because " +
                    "it has not been published yet", id));

        return (EventFullOutDto) addConfirmedRequestsAndViews(List.of(event), true).get(0);
    }

    private BooleanExpression getFinalCondition(String text, int[] categories, Boolean paid, LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd, boolean onlyAvailable) {
        List<BooleanExpression> conditions = new ArrayList<>();
        QEvent event = QEvent.event;

        conditions.add(event.state.eq(State.PUBLISHED).and(event.eventDate.after(rangeStart == null? LocalDateTime.now()
                : rangeStart)));
        if(text != null)
            conditions.add(event.annotation.containsIgnoreCase(text).or(event.description.containsIgnoreCase(text)));
        if(categories != null) {
            List<Long> catIds = Arrays.stream(categories).mapToObj(Long::valueOf).toList();
            conditions.add(event.category.id.in(catIds));
        }
        if(paid != null) conditions.add(paid ? event.paid.isTrue() : event.paid.isFalse());
        if(rangeEnd != null) conditions.add(event.eventDate.before(rangeEnd));
        if(onlyAvailable) {
            QRequest request = QRequest.request;
            BooleanExpression ifLimitIsZero = event.participantLimit.eq(0);
            BooleanExpression ifRequestModerationFalse = event.requestModeration.isFalse()
                    .and(event.participantLimit.goe(request.count()));
            BooleanExpression ifRequestModerationTrue = event.requestModeration.isTrue()
                    .and(event.participantLimit.goe(request.status.eq(Status.CONFIRMED).count()));
            conditions.add(ifLimitIsZero.or(ifRequestModerationFalse).or(ifRequestModerationTrue));
        }

        return conditions.stream()
                .reduce(BooleanExpression::and)
                .get();
    }

    private List<EventShortOutDto> getEventsSortedByDate(BooleanExpression expression, int from, int size) {
        Pageable pageable = Pagination.of(from, size, Sort.by("eventDate").descending());
        Slice<Event> events = eventRepository.findAll(expression, pageable);

        return addConfirmedRequestsAndViews(events.getContent(), false).stream()
                .map(eventOutDto -> (EventShortOutDto) eventOutDto)
                .collect(Collectors.toList());
    }

    private List<EventShortOutDto> getEventsSortedByViews(BooleanExpression expression, int from, int size) {
        List<Event> events = new ArrayList<>();
        eventRepository.findAll(expression).forEach(events::add);

        return addConfirmedRequestsAndViews(events, false).stream()
                .map(eventOutDto -> (EventShortOutDto) eventOutDto)
                .sorted(Comparator.comparing(EventShortOutDto::getViews))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }
}
