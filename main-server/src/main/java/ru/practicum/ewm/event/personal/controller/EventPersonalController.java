package ru.practicum.ewm.event.personal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.event.personal.service.EventPersonalService;
import ru.practicum.ewm.request.model.dto.RequestOutDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequestMapping("/users/{userId}/events")
public class EventPersonalController {
    private final EventPersonalService eventPersonalService;

    @Autowired
    public EventPersonalController(EventPersonalService eventPersonalService) {
        this.eventPersonalService = eventPersonalService;
    }

    @GetMapping
    public List<EventShortOutDto> getEvents(@PathVariable @Positive(message = "The value must be greater than 0")
                                                long userId,
                                            @RequestParam(value = "from", defaultValue = "0")
                                            @PositiveOrZero(message = "The from must be greater than or equal to 0")
                                                int from,
                                            @RequestParam(value = "size", defaultValue = "10")
                                            @Min(value = 1, message = "The min allowed value for the size is 1")
                                                int size) {
        return eventPersonalService.getEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventOutDto getEvent(@PathVariable @Positive(message = "The value {userId} must be greater than 0")
                                        long userId,
                                    @PathVariable @Positive(message = "The value {eventId} must be greater than 0")
                                        long eventId) {
        return eventPersonalService.getEventById(userId, eventId);
    }

    @PatchMapping
    public EventOutDto updateEvent(@PathVariable @Positive(message = "The value must be greater than 0") long userId,
                                       @RequestBody @Valid EventChangedDto eventChangedDto) {
        return eventPersonalService.updateEvent(userId, eventChangedDto);
    }

    @PostMapping
    public EventFullOutDto addEvent(@PathVariable @Positive(message = "The value must be greater than 0") long userId,
                                    @RequestBody @Valid EventInDto eventInDto) {
        return eventPersonalService.createEvent(userId, eventInDto);
    }

    @PatchMapping("/{eventId}")
    public EventOutDto cancelEvent(@PathVariable @Positive(message = "The value {userId} must be greater than 0")
                                           long userId,
                                       @PathVariable @Positive(message = "The value {eventId} must be greater than 0")
                                           long eventId) {
        return eventPersonalService.cancelEvent(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestOutDto> getRequests(@PathVariable @Positive(message = "The value {userId} must be greater than 0")
                                               long userId,
                                           @PathVariable @Positive(message = "The value {eventId} must be greater than 0")
                                               long eventId) {
        return eventPersonalService.getRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/confirm")
    public RequestOutDto confirmRequest(@PathVariable @Positive(message = "The value {userId} must be greater than 0")
                                            long userId,
                                        @PathVariable @Positive(message = "The value {eventId} must be greater than 0")
                                            long eventId,
                                        @PathVariable @Positive(message = "The value {reqId} must be greater than 0")
                                            long reqId) {
        return eventPersonalService.confirmRequest(userId, eventId, reqId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/reject")
    public RequestOutDto rejectRequest(@PathVariable @Positive(message = "The value {userId} must be greater than 0")
                                           long userId,
                                       @PathVariable @Positive(message = "The value {eventId} must be greater than 0")
                                           long eventId,
                                       @PathVariable @Positive(message = "The value {reqId} must be greater than 0")
                                           long reqId) {
        return eventPersonalService.rejectRequest(userId, eventId, reqId);
    }
}
