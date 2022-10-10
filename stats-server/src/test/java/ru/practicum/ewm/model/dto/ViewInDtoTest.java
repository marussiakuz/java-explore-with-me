package ru.practicum.ewm.model.dto;

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
class ViewInDtoTest {
    @Autowired
    JacksonTester<ViewInDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testViewInDto() throws IOException {
        LocalDateTime timestamp = LocalDateTime.now();

        ViewInDto view = ViewInDto.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/1")
                .ip("0.0.0.0.0.1")
                .timestamp(timestamp)
                .build();

        JsonContent<ViewInDto> result = json.write(view);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-server");
        assertThat(result).extractingJsonPathStringValue("$.uri")
                .isEqualTo("http://localhost:8080/events/1");
        assertThat(result).extractingJsonPathStringValue("$.ip").isEqualTo("0.0.0.0.0.1");
        assertThat(result).extractingJsonPathStringValue("$.timestamp")
                .isEqualTo(timestamp.format(DATE_TIME_FORMATTER));
    }
}