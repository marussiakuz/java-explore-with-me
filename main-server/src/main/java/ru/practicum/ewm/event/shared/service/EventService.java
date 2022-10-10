package ru.practicum.ewm.event.shared.service;

import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortOutDto> getEvents(String text, int[] categories, Boolean paid, LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd, boolean onlyAvailable, String sortingEvents,
                                     int from, int size);

    EventFullOutDto getEventById(long id);
}
