package ru.practicum.ewm.compilation.model.mapper;

import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import java.util.List;

public class CompilationMapper {

    public static CompilationOutDto toCompilationOut(Compilation compilation, List<EventShortOutDto> events) {
        return CompilationOutDto.builder()
                .id(compilation.getId())
                .events(events)
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static Compilation toCompilation(CompilationInDto compilation, List<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .build();
    }
}
