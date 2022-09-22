package ru.practicum.ewm.compilation.shared.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.model.CompilationOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import java.util.List;

@Service
public class CompilationServiceImpl implements CompilationService {
    @Override
    public List<CompilationOutDto> getCompilations(boolean pinned, int from, int size) {
        return null;
    }

    @Override
    public List<EventShortOutDto> getCompilationById(long compilationId) {
        return null;
    }
}
