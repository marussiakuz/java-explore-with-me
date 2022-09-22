package ru.practicum.ewm.request.personal.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.request.personal.model.dto.RequestOutDto;

import java.util.List;

@Service
public class RequestServiceImpl implements RequestService {
    @Override
    public RequestOutDto createRequest(long userId, long eventId) {
        return null;
    }

    @Override
    public RequestOutDto cancelRequest(long userId, long requestId) {
        return null;
    }

    @Override
    public List<RequestOutDto> getRequests(long userId) {
        return null;
    }
}
