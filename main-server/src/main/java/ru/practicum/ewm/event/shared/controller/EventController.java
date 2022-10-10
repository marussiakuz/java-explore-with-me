package ru.practicum.ewm.event.shared.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.event.enums.SortingEvents;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.shared.service.EventService;
import ru.practicum.ewm.util.validator.ValueOfEnum;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;
    private final EventStatClient eventStatClient;

    @Autowired
    public EventController(EventService eventService, EventStatClient eventStatClient) {
        this.eventService = eventService;
        this.eventStatClient = eventStatClient;
    }

    @GetMapping
    public List<EventShortOutDto> getEvents(@RequestParam(value = "text", required = false) String text,
                                            @RequestParam(value = "categories", required = false) int[] categories,
                                            @RequestParam(value = "paid", required = false) Boolean paid,
                                            @RequestParam(value = "rangeStart", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(value = "rangeEnd", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(value = "onlyAvailable", required = false,
                                                    defaultValue = "false") boolean onlyAvailable,
                                            @RequestParam(value = "sort", required = false, defaultValue = "EVENT_DATE")
                                                @ValueOfEnum(enumClass = SortingEvents.class, isNullEnabled = true,
                                                    message = "Unsupported sorting value") String sort,
                                            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
                                            HttpServletRequest request) {
        eventStatClient.sendViewToStatsServer(request);
        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullOutDto getEvent(@Positive @PathVariable long id, HttpServletRequest request) {
        eventStatClient.sendViewToStatsServer(request);
        return eventService.getEventById(id);
    }
}
