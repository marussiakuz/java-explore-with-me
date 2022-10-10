package ru.practicum.ewm.client.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ViewStatsDtoTest {
    @Autowired
    JacksonTester<ViewStatsDto> json;

    @Test
    void testViewStatsDto() throws IOException {
        ViewStatsDto view = ViewStatsDto.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/1")
                .hits(100)
                .build();

        JsonContent<ViewStatsDto> result = json.write(view);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-server");
        assertThat(result).extractingJsonPathStringValue("$.uri")
                .isEqualTo("http://localhost:8080/events/1");
        assertThat(result).extractingJsonPathNumberValue("$.hits").isEqualTo(100);
    }
}