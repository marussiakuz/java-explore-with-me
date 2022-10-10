package ru.practicum.ewm.event.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class CommentOutDto {
    private long id;
    private String text;
    private LocalDateTime created;
}
