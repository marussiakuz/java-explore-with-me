package ru.practicum.ewm.user.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class UserShortOutDtoTest {
    @Autowired
    JacksonTester<UserShortOutDto> json;

    @Test
    void testUserShortOutDto() throws IOException {
        UserShortOutDto user = UserShortOutDto.builder()
                .id(120L)
                .name("Vasya")
                .build();

        JsonContent<UserShortOutDto> result = json.write(user);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(120);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Vasya");
    }
}