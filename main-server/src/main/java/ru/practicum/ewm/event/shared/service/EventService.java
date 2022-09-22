package ru.practicum.ewm.event.shared.service;

import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import java.util.List;

public interface EventService {

    List<EventShortOutDto> getEvents(String text, int[] categories, boolean paid, String rangeStart, String rangeEnd,
                                     boolean onlyAvailable, String sort, int from, int size);
    EventFullOutDto getEventById(long id);
}
