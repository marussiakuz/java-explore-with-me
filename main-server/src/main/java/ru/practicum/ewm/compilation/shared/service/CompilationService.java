package ru.practicum.ewm.compilation.shared.service;

import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;

import java.util.List;

public interface CompilationService {

    List<CompilationOutDto> getCompilations(Boolean pinned, int from, int size);

    CompilationOutDto getCompilationById(long compilationId);
}
