package ru.practicum.ewm.request.personal.service;

import ru.practicum.ewm.request.personal.model.dto.RequestOutDto;

import java.util.List;

public interface RequestService {
    RequestOutDto createRequest(long userId, long eventId);
    RequestOutDto cancelRequest(long userId, long requestId);
    List<RequestOutDto> getRequests(long userId);
}
