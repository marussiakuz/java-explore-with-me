package ru.practicum.ewm.compilation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationOutDto {
    private long id;
    private String title;
    private boolean pinned;
    private List<EventShortOutDto> events;
}
