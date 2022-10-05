package ru.practicum.ewm.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInDto {
    @NotBlank(message = "Name must not be blank")
    private String name;
    @Email(message = "The email is incorrect")
    private String email;
}
