package ru.practicum.ewm.category.model.dto;

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
public class CategoryChangedDto {
    private long id;
    @NotBlank(message = "The name of the category must not be blank")
    @Size(min = 1, max = 64, message = "The name of the category must be between 1 and 64 characters long")
    private String name;
}
