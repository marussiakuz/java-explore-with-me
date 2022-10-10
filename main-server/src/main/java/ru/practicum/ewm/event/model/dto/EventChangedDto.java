package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.util.validator.IsLaterFromTheCurrentTime;
import ru.practicum.ewm.util.validator.NullOrNotBlank;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventChangedDto {
    private Long id;
    @NullOrNotBlank(message = "Annotation must not be blank")
    @Size(min = 1, max = 512, message = "Annotation must be between 1 and 512 characters long")
    private String annotation;
    @NullOrNotBlank(message = "Description must not be blank")
    @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters long")
    private String description;
    private Long category;
    @NullOrNotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 512, message = "Title must be between 1 and 512 characters long")
    private String title;
    private Integer participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @IsLaterFromTheCurrentTime(isNullable = true, message = "The event must not take place earlier than two hours " +
            "from the current time")
    private LocalDateTime eventDate;
    private Boolean paid;
}
