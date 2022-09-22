package ru.practicum.ewm.event.personal.service;

import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.request.personal.model.dto.RequestOutDto;

import java.util.List;

public interface EventPersonalService {
    List<EventShortOutDto> getEvents(long userId, int from, int size);
    EventFullOutDto getEventById(long userId, long id);
    EventFullOutDto updateEvent(long userId, EventChangedDto eventChangedDto);
    EventFullOutDto createEvent(long userId, EventInDto eventInDto);
    EventFullOutDto cancelEvent(long userId, long eventId);
    RequestOutDto getRequests(long userId, long eventId);
    RequestOutDto confirmRequest(long userId, long eventId, long reqId);
    RequestOutDto rejectRequest(long userId, long eventId, long reqId);

}
