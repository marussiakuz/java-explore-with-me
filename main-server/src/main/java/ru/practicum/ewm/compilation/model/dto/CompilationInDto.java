package ru.practicum.ewm.compilation.model.dto;

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
public class CompilationInDto {
    private long[] events;
    private boolean pinned;
    @NotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 512, message = "Title must be between 1 and 512 characters long")
    private String title;
}
