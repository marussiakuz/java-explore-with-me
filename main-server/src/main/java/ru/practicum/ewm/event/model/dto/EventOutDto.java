package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public abstract class EventOutDto {
    private Long id;
    private String annotation;
    private CategoryOutDto category;
    private UserShortOutDto initiator;
    private String title;
    private int confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private boolean paid;
    private long views;
}
