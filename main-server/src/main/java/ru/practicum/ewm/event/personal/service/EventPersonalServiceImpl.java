package ru.practicum.ewm.event.personal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.request.personal.model.dto.RequestOutDto;

import java.util.List;

@Service
@Slf4j
public class EventPersonalServiceImpl implements EventPersonalService {
    @Override
    public List<EventShortOutDto> getEvents(long userId, int from, int size) {
        return null;
    }

    @Override
    public EventFullOutDto getEventById(long userId, long id) {
        return null;
    }

    @Override
    public EventFullOutDto updateEvent(long userId, EventChangedDto eventChangedDto) {
        return null;
    }

    @Override
    public EventFullOutDto createEvent(long userId, EventInDto eventInDto) {
        return null;
    }

    @Override
    public EventFullOutDto cancelEvent(long userId, long eventId) {
        return null;
    }

    @Override
    public RequestOutDto getRequests(long userId, long eventId) {
        return null;
    }

    @Override
    public RequestOutDto confirmRequest(long userId, long eventId, long reqId) {
        return null;
    }

    @Override
    public RequestOutDto rejectRequest(long userId, long eventId, long reqId) {
        return null;
    }
}
