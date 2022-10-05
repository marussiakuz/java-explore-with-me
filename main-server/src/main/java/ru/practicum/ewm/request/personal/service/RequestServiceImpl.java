package ru.practicum.ewm.request.personal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.error.handler.exception.RequestNotFoundException;
import ru.practicum.ewm.error.handler.exception.UserNotFoundException;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.model.mapper.RequestMapper;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public RequestOutDto createRequest(long userId, long eventId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId))
            throw new ConditionIsNotMetException(String.format("User with id=%s already has a request to participate " +
                    "in event with id=%s", userId, eventId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", eventId)));
        checkEventForRestrictions(event, userId);

        Request request = Request.builder()
                .requester(userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", userId))))
                .event(event)
                .status(event.isRequestModeration() ? Status.PENDING : Status.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        Request saved = requestRepository.save(request);
        log.info("request id={} has been successfully added with status-{}", saved.getId(), saved.getStatus());
        return RequestMapper.toRequestOut(saved);
    }

    @Override
    public RequestOutDto cancelRequest(long userId, long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(String.format("Request with id=%s not found", requestId)));

        if (request.getRequester().getId() != userId)
            throw new ConditionIsNotMetException("the request belongs to another user");

        request.setStatus(Status.CANCELED);
        Request updated = requestRepository.save(request);
        log.info("status of request id={} changed to CANCELED", requestId);

        return RequestMapper.toRequestOut(updated);
    }

    @Override
    public List<RequestOutDto> getRequests(long userId) {
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toRequestOut)
                .collect(Collectors.toList());
    }

    private void checkEventForRestrictions(Event event, long userId) {
        if (event.getInitiator().getId() == userId)
            throw new ConditionIsNotMetException("The initiator of the event cannot create a request to participate " +
                    "in his own event");

        if (event.getState() != State.PUBLISHED)
            throw new ConditionIsNotMetException("cannot apply to participate in an unpublished event");

        int limit = event.getParticipantLimit();

        if (limit > 0) {
            int confirmedRequests = (int) requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);
            if (confirmedRequests == limit)
                throw new ConditionIsNotMetException(String.format("The event with id=%s has already reached " +
                        "the request limit", event.getId()));
        }

        log.info("the event id={} has no restrictions for request to participate in it", event.getId());
    }
}
