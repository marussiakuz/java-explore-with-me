package ru.practicum.ewm.compilation.shared.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.compilation.shared.service.CompilationService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequestMapping("/compilations")
public class CompilationController {
    private final CompilationService compilationService;

    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    public List<CompilationOutDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(value = "from", defaultValue = "0")
                                                   @PositiveOrZero(message = "The from must be greater than or " +
                                                       "equal to 0") int from,
                                                   @RequestParam(value = "size", defaultValue = "10")
                                                   @Min(value = 1, message = "The min allowed value for the size is 1")
                                                       int size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationOutDto getCompilation(@PathVariable @Positive(message = "The value must be greater than 0")
                                                int compId) {
        return compilationService.getCompilationById(compId);
    }
}
