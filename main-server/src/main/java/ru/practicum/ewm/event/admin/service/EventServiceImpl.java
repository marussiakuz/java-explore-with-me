package ru.practicum.ewm.event.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.model.dto.EventAdminChangedDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;

import java.util.List;

@Service
@Slf4j
public class EventServiceImpl implements EventService {
    @Override
    public List<EventFullOutDto> getEvents(int[] users, String[] states, int[] categories, String rangeStart, String rangeEnd, int from, int size) {
        return null;
    }

    @Override
    public EventFullOutDto updateEvent(long eventId, EventAdminChangedDto eventAdminChangedDto) {
        return null;
    }

    @Override
    public EventFullOutDto publishEvent(long eventId) {
        return null;
    }

    @Override
    public EventFullOutDto rejectEvent(long eventId) {
        return null;
    }
}
