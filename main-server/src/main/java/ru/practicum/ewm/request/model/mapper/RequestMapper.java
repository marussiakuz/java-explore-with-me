package ru.practicum.ewm.request.model.mapper;

import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.dto.RequestOutDto;

public class RequestMapper {
    public static RequestOutDto toRequestOut(Request request) {
        return RequestOutDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(request.getCreated())
                .status(request.getStatus())
                .build();
    }
}
