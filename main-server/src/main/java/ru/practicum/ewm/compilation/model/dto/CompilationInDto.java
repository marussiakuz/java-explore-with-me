package ru.practicum.ewm.compilation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationInDto {
    private long[] events;
    private boolean pinned;
    @NotBlank(message = "Title must not be blank")
    private String title;
}
