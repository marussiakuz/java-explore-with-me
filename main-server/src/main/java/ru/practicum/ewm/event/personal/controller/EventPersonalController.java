package ru.practicum.ewm.event.personal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.event.personal.service.EventPersonalService;
import ru.practicum.ewm.request.personal.model.dto.RequestOutDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
public class EventPersonalController {
    private final EventPersonalService eventPersonalService;

    @Autowired
    public EventPersonalController(EventPersonalService eventPersonalService) {
        this.eventPersonalService = eventPersonalService;
    }

    @GetMapping
    public List<EventShortOutDto> getEvents(@PathVariable @Positive long userId,
                                            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        return eventPersonalService.getEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullOutDto getEvent(@PathVariable @Positive long userId,
                                    @PathVariable @Positive long eventId) {
        return eventPersonalService.getEventById(userId, eventId);
    }

    @PatchMapping
    public EventFullOutDto updateEvent(@PathVariable @Positive long userId,
                                       @RequestBody EventChangedDto eventChangedDto) {
        return eventPersonalService.updateEvent(userId, eventChangedDto);
    }

    @PostMapping
    public EventFullOutDto addEvent(@PathVariable @Positive long userId,
                                    @RequestBody EventInDto eventInDto) {
        return eventPersonalService.createEvent(userId, eventInDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullOutDto cancelEvent(@PathVariable @Positive long userId,
                                       @PathVariable @Positive long eventId) {
        return eventPersonalService.cancelEvent(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public RequestOutDto getRequests(@PathVariable @Positive long userId,
                                     @PathVariable @Positive long eventId) {
        return eventPersonalService.getRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/confirm")
    public RequestOutDto confirmRequest(@PathVariable @Positive long userId,
                                          @PathVariable @Positive long eventId,
                                          @PathVariable @Positive long reqId) {
        return eventPersonalService.confirmRequest(userId, eventId, reqId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/reject")
    public RequestOutDto rejectRequest(@PathVariable @Positive long userId,
                                        @PathVariable @Positive long eventId,
                                        @PathVariable @Positive long reqId) {
        return eventPersonalService.rejectRequest(userId, eventId, reqId);
    }
}
