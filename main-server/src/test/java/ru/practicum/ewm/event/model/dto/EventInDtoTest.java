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
class EventInDtoTest {
    @Autowired
    JacksonTester<EventInDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testEventInDto() throws IOException {
        EventInDto event = EventInDto.builder()
                .paid(true)
                .category(23)
                .requestModeration(true)
                .participantLimit(20)
                .annotation("Annotation")
                .description("description")
                .location(LocationDto.builder()
                        .latitude(46.4546f)
                        .longitude(52.5483f)
                        .build())
                .eventDate(LocalDateTime.parse("2022-11-11 12:30:00", DATE_TIME_FORMATTER))
                .title("very interesting event")
                .build();

        JsonContent<EventInDto> result = json.write(event);

        assertThat(result).extractingJsonPathNumberValue("$.category").isEqualTo(23);
        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("Annotation");
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo("2022-11-11 12:30:00");
        assertThat(result).extractingJsonPathNumberValue("$.location.lon").isEqualTo(52.5483);
        assertThat(result).extractingJsonPathNumberValue("$.location.lat").isEqualTo(46.4546);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(20);
        assertThat(result).extractingJsonPathBooleanValue("$.requestModeration").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("very interesting event");
    }
}