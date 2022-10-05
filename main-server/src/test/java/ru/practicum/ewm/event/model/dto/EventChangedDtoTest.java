package ru.practicum.ewm.event.model.dto;

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
class EventChangedDtoTest {
    @Autowired
    JacksonTester<EventChangedDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testEventChangedDto() throws IOException {
        LocalDateTime timestamp = LocalDateTime.now();

        EventChangedDto event = EventChangedDto.builder()
                .id(45L)
                .category(13L)
                .annotation("Annotation")
                .eventDate(timestamp)
                .description("description")
                .paid(true)
                .participantLimit(100)
                .title("title")
                .build();

        JsonContent<EventChangedDto> result = json.write(event);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(45);
        assertThat(result).extractingJsonPathNumberValue("$.category").isEqualTo(13);
        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("Annotation");
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo(timestamp.format(DATE_TIME_FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");
    }
}