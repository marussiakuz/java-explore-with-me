package ru.practicum.ewm.request.personal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.model.dto.RequestOutDto;
import ru.practicum.ewm.request.personal.service.RequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Validated
@RequestMapping("/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public RequestOutDto addRequest(@PathVariable @Positive long userId,
                                    @RequestParam @Positive long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestOutDto cancelRequest(@PathVariable @Positive long userId,
                                       @PathVariable @Positive long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping
    public List<RequestOutDto> getRequests(@PathVariable @Positive long userId) {
        return requestService.getRequests(userId);
    }
}
