package ru.practicum.ewm.client.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class HitDtoTest {
    @Autowired
    JacksonTester<HitDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testHitDto() throws IOException {
        LocalDateTime timestamp = LocalDateTime.now();

        HitDto hitDto = HitDto.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/1")
                .ip("0.0.0.0.0.1")
                .timestamp(timestamp)
                .build();

        JsonContent<HitDto> result = json.write(hitDto);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-server");
        assertThat(result).extractingJsonPathStringValue("$.uri")
                .isEqualTo("http://localhost:8080/events/1");
        assertThat(result).extractingJsonPathStringValue("$.ip").isEqualTo("0.0.0.0.0.1");
        assertThat(result).extractingJsonPathStringValue("$.timestamp")
                .isEqualTo(timestamp.format(DATE_TIME_FORMATTER));
    }
}