package ru.practicum.ewm.event.shared.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.shared.service.EventService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventShortOutDto> getEvents(@RequestParam(value = "text", required = false) String text,
                                            @RequestParam(value = "categories", required = false) int[] categories,
                                            @RequestParam(value = "paid", required = false) boolean paid,
                                            @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                            @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                            @RequestParam(value = "onlyAvailable", required = false, defaultValue = "false")
                                             boolean onlyAvailable,
                                            @RequestParam(value = "sort", required = false) String sort,
                                            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullOutDto getEvent(@Positive @PathVariable long id) {
        return eventService.getEventById(id);
    }
}
