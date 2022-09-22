package ru.practicum.ewm.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAdminChangedDto {

    private String annotation;
    private String description;
    private int category;
    private String title;
    private int participantLimit;
    private LocalDateTime eventDate;
    private LocationDto locationDto;
    private int requestModeration;
    private boolean paid;
}
