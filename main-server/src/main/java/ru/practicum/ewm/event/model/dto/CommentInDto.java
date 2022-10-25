package ru.practicum.ewm.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentInDto {
    @NotBlank(message = "Text must not be blank")
    @Size(min = 1, max = 1000, message = "Text must be between 1 and 1000 characters long")
    private String text;
}
