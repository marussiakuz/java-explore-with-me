package ru.practicum.ewm.compilation.admin.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.admin.service.CompilationAdminService;
import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/admin/compilations")
public class CompilationAdminController {
    private final CompilationAdminService compilationAdminService;

    public CompilationAdminController(CompilationAdminService compilationAdminService) {
        this.compilationAdminService = compilationAdminService;
    }

    @PostMapping
    public CompilationOutDto addCompilation(@RequestBody @Valid CompilationInDto compilationInDto) {
        return compilationAdminService.createCompilation(compilationInDto);
    }

    @DeleteMapping("/{compId}")
    public void deleteCompilation(@PathVariable @Positive(message = "The value must be greater than 0") long compId) {
        compilationAdminService.deleteCompilation(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void deleteEvent(@PathVariable @Positive(message = "The value {compId} must be greater than 0") long compId,
                            @PathVariable @Positive(message = "The value {eventId} must be greater than 0")
                                long eventId) {
        compilationAdminService.deleteEvent(compId, eventId);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void addEvent(@PathVariable @Positive(message = "The value {compId} must be greater than 0") long compId,
                         @PathVariable @Positive(message = "The value {eventId} must be greater than 0") long eventId) {
        compilationAdminService.addEvent(compId, eventId);
    }

    @DeleteMapping("/{compId}/pin")
    public void unpinCompilation(@PathVariable @Positive(message = "The value must be greater than 0") long compId) {
        compilationAdminService.unpinCompilation(compId);
    }

    @PatchMapping("/{compId}/pin")
    public void pinCompilation(@PathVariable @Positive(message = "The value must be greater than 0") long compId) {
        compilationAdminService.pinCompilation(compId);
    }
}
