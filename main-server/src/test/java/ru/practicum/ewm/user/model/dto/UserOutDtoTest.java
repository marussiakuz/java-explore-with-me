package ru.practicum.ewm.user.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class UserOutDtoTest {
    @Autowired
    JacksonTester<UserOutDto> json;

    @Test
    void testUserOutDto() throws IOException {
        UserOutDto user = UserOutDto.builder()
                .id(120L)
                .email("cats@gmail.com")
                .name("Vasya")
                .build();

        JsonContent<UserOutDto> result = json.write(user);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(120);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("cats@gmail.com");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Vasya");
    }
}