package ru.practicum.ewm.event.admin.service;

import ru.practicum.ewm.event.model.dto.EventAdminChangedDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;

import java.util.List;

public interface EventService {

    List<EventFullOutDto> getEvents(int[] users, String[] states, int[] categories, String rangeStart, String rangeEnd,
                                    int from, int size);

    EventFullOutDto updateEvent(long eventId, EventAdminChangedDto eventAdminChangedDto);
    EventFullOutDto publishEvent(long eventId);
    EventFullOutDto rejectEvent(long eventId);
}
