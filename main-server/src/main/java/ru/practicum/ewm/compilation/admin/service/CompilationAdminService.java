package ru.practicum.ewm.compilation.admin.service;

import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;

public interface CompilationAdminService {

    CompilationOutDto createCompilation(CompilationInDto compilationInDto);

    void deleteCompilation(long compId);

    void deleteEvent(long compId, long eventId);

    void addEvent(long compId, long eventId);

    void unpinCompilation(long compId);

    void pinCompilation(long compId);
}
