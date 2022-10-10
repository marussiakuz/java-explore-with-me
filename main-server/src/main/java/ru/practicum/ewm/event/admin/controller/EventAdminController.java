package ru.practicum.ewm.event.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.admin.service.EventAdminService;
import ru.practicum.ewm.event.model.dto.EventAdminChangedDto;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequestMapping("/admin/events")
public class EventAdminController {
    private final EventAdminService eventService;

    @Autowired
    public EventAdminController(EventAdminService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventFullOutDto> getEvents(@RequestParam(value = "users", required = false) int[] users,
                                           @RequestParam(value = "states", required = false) String[] states,
                                           @RequestParam(value = "categories", required = false) int[] categories,
                                           @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                           @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                           @RequestParam(value = "from", defaultValue = "0")
                                           @PositiveOrZero(message = "The from must be greater than or equal to 0")
                                               int from,
                                           @RequestParam(value = "size", defaultValue = "10")
                                           @Min(value = 1, message = "The min allowed value for the size is 1")
                                               int size) {
        return eventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PutMapping("/{eventId}")
    public EventFullOutDto updateEvent(@PathVariable @Positive(message = "The value must be greater than 0")
                                           long eventId,
                                       @RequestBody EventAdminChangedDto eventAdminChangedDto) {
        return eventService.updateEvent(eventId, eventAdminChangedDto);
    }

    @PatchMapping("/{eventId}/publish")
    public EventFullOutDto publishEvent(@PathVariable @Positive(message = "The value must be greater than 0")
                                            long eventId) {
        return eventService.publishEvent(eventId);
    }

    @PatchMapping("/{eventId}/reject")
    public EventFullOutDto rejectEvent(@PathVariable @Positive(message = "The value must be greater than 0")
                                           long eventId) {
        return eventService.rejectEvent(eventId);
    }
}
