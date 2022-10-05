package ru.practicum.ewm.category.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryChangedDto {
    private long id;
    @NotBlank(message = "Name must not be blank")
    private String name;
}
