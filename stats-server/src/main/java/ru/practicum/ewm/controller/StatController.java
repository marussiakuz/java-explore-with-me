package ru.practicum.ewm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.practicum.ewm.model.dto.ViewInDto;
import ru.practicum.ewm.model.dto.ViewOutDto;
import ru.practicum.ewm.service.StatService;

import java.util.List;

@RestController
@RequestMapping
public class StatController {
    private final StatService statService;

    @Autowired
    public StatController(StatService statService) {
        this.statService = statService;
    }

    @PostMapping("/hit")
    public void addView(@RequestBody ViewInDto viewInDto) {
        statService.saveView(viewInDto);
    }

    @GetMapping("/stats")
    public List<ViewOutDto> getStats(@RequestParam(value = "start", required = false) String start,
                                     @RequestParam(value = "end", required = false) String end,
                                     @RequestParam(required = false) String[] uris,
                                     @RequestParam(defaultValue = "false") boolean unique) {
        return statService.getStats(start, end, uris, unique);
    }
}
