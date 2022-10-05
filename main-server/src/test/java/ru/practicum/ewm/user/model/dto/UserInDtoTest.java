package ru.practicum.ewm.user.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class UserInDtoTest {
    @Autowired
    JacksonTester<UserInDto> json;

    @Test
    void testUserInDto() throws IOException {
        UserInDto user = UserInDto.builder()
                .email("cats@gmail.com")
                .name("Vasya")
                .build();

        JsonContent<UserInDto> result = json.write(user);

        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("cats@gmail.com");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Vasya");
    }
}