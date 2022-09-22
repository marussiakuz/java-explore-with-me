package ru.practicum.ewm.compilation.shared.service;

import ru.practicum.ewm.compilation.model.CompilationOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import java.util.List;

public interface CompilationService {
    List<CompilationOutDto> getCompilations(boolean pinned, int from, int size);
    List<EventShortOutDto> getCompilationById(long compilationId);
}
