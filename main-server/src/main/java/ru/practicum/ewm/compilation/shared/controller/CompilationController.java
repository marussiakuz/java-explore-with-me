package ru.practicum.ewm.compilation.shared.controller;

import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.model.CompilationOutDto;
import ru.practicum.ewm.compilation.shared.service.CompilationService;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/compilations")
public class CompilationController {
    private final CompilationService compilationService;


    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    public List<CompilationOutDto> getCompilations(@RequestParam(required = false) boolean pinned,
                                                   @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                                   @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public List<EventShortOutDto> getCompilation(@PathVariable @Positive int compId) {
        return compilationService.getCompilationById(compId);
    }
}
