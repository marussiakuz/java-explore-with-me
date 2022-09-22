package ru.practicum.ewm.compilation.admin.controller;

import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.model.CompilationInDto;
import ru.practicum.ewm.compilation.model.CompilationOutDto;
import ru.practicum.ewm.compilation.admin.service.CompilationService;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/compilations")
public class CompilationController {
    private final CompilationService compilationService;


    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    public CompilationOutDto addCompilation(@RequestBody CompilationInDto compilationInDto) {
        return compilationService.createCompilation(compilationInDto);
    }

    @DeleteMapping("/{compId}")
    public void deleteCompilation(@PathVariable long compId) {
        compilationService.deleteCompilation(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void deleteEvent(@PathVariable @Positive long compId,
                            @PathVariable @Positive long eventId) {
        compilationService.deleteEvent(compId, eventId);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void addEvent(@PathVariable @Positive long compId,
                         @PathVariable @Positive long eventId) {
        compilationService.addEvent(compId, eventId);
    }

    @DeleteMapping("/{compId}/pin")
    public void unpinCompilation(@PathVariable @Positive long compId) {
        compilationService.unpinCompilation(compId);
    }

    @PatchMapping("/{compId}/pin")
    public void pinCompilation(@PathVariable @Positive long compId) {
        compilationService.pinCompilation(compId);
    }
}
