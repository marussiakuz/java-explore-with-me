package ru.practicum.ewm.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInDto {
    @NotBlank(message = "Name must not be blank")
    @Size(min = 1, max = 256, message = "The username must be between 1 and 256 characters long")
    private String name;
    @Email(message = "The email is incorrect")
    private String email;
}
