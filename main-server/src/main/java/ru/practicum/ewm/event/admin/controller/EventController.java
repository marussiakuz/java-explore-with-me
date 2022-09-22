package ru.practicum.ewm.event.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.admin.service.EventServiceImpl;
import ru.practicum.ewm.event.model.dto.EventAdminChangedDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
public class EventController {

    private final EventServiceImpl eventService;

    @Autowired
    public EventController(EventServiceImpl eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventFullOutDto> getEvents(@RequestParam(value = "users", required = false) int[] users,
                                           @RequestParam(value = "states", required = false) String[] states,
                                           @RequestParam(value = "categories", required = false) int[] categories,
                                           @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                           @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                           @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                           @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        return eventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PutMapping("/{eventId}")
    public EventFullOutDto updateEvent(@PathVariable @Positive long eventId,
                                       @RequestBody EventAdminChangedDto eventAdminChangedDto) {
        return eventService.updateEvent(eventId, eventAdminChangedDto);
    }

    @PatchMapping("{eventId}/publish")
    public EventFullOutDto publishEvent(@PathVariable @Positive long eventId) {
        return eventService.publishEvent(eventId);
    }

    @PatchMapping("{eventId}/reject")
    public EventFullOutDto rejectEvent(@PathVariable @Positive long eventId) {
        return eventService.rejectEvent(eventId);
    }
}
