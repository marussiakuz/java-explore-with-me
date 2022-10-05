package ru.practicum.ewm.client.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.request.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class StatisticEventService {
    private final EventStatClient eventStatClient;
    protected final RequestRepository requestRepository;

    protected List<EventOutDto> addConfirmedRequestsAndViews(List<Event> events, boolean isFull) {
        if(events.isEmpty()) {
            log.info("an empty list of events has been passed");
            return new ArrayList<>();
        }

        Map<Long, Long> eventViews = eventStatClient.getStatisticOnViews(events, true);
        log.debug("found statistic on events, count={}", eventViews.size());

        AtomicLong hits = new AtomicLong();
        return events.stream()
                .peek(event -> hits.set(eventViews.get(event.getId()) == null ? 0 : eventViews.get(event.getId())))
                .map(event -> isFull ? mapToEventFull(event, hits.get()) : mapToEventShort(event, hits.get()))
                .collect(Collectors.toList());
    }

    private EventShortOutDto mapToEventShort(Event event, long views) {
        return EventMapper.toEventShort(event, event.isRequestModeration() ?
                (int) requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED)
                : (int) requestRepository.countByEventId(event.getId()), views);
    }

    private EventFullOutDto mapToEventFull(Event event, long views) {
        return EventMapper.toEventFull(event, event.isRequestModeration() ?
                (int) requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED)
                : (int) requestRepository.countByEventId(event.getId()), views);
    }
}
