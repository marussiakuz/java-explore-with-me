package ru.practicum.ewm.compilation.admin.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.model.CompilationInDto;
import ru.practicum.ewm.compilation.model.CompilationOutDto;

@Service
public class CompilationServiceImpl implements CompilationService {
    @Override
    public CompilationOutDto createCompilation(CompilationInDto compilationInDto) {
        return null;
    }

    @Override
    public void deleteCompilation(long compId) {

    }

    @Override
    public void deleteEvent(long compId, long eventId) {

    }

    @Override
    public void addEvent(long compId, long eventId) {

    }

    @Override
    public void unpinCompilation(long compId) {

    }

    @Override
    public void pinCompilation(long compId) {

    }
}
