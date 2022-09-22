package ru.practicum.ewm.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortOutDto {

    private Long id;
    private String annotation;
    private Category category;
    private User initiator;
    private String title;
    private int confirmedRequests;
    private LocalDateTime eventDate;
    private boolean paid;
    private long views;
}
