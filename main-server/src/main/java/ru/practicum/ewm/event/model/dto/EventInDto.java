package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.util.validator.IsLaterFromTheCurrentTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventInDto {
    @NotBlank(message = "Annotation must not be blank")
    @Size(min = 1, max = 560, message = "Annotation must be between 1 and 560 characters long")
    private String annotation;
    private long category;
    private LocationDto location;
    @NotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 260, message = "Title must be between 1 and 260 characters long")
    private String title;
    @NotBlank(message = "Description must not be blank")
    @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters long")
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @IsLaterFromTheCurrentTime(message = "The event must not take place earlier than two hours from the current time")
    private LocalDateTime eventDate;
    private boolean paid;
    private int participantLimit;
    private boolean requestModeration;
}
