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
class EventAdminChangedDtoTest {
    @Autowired
    JacksonTester<EventAdminChangedDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testEventAdminChangedDto() throws IOException {
        LocalDateTime timestamp = LocalDateTime.now();

        EventAdminChangedDto event = EventAdminChangedDto.builder()
                .category(13L)
                .annotation("Annotation")
                .eventDate(timestamp)
                .location(LocationDto.builder().longitude(23.2332f).latitude(123.2134f).build())
                .description("description")
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .title("title")
                .build();

        JsonContent<EventAdminChangedDto> result = json.write(event);

        assertThat(result).extractingJsonPathNumberValue("$.category").isEqualTo(13);
        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("Annotation");
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo(timestamp.format(DATE_TIME_FORMATTER));
        assertThat(result).extractingJsonPathNumberValue("$.location.lon").isEqualTo(23.2332);
        assertThat(result).extractingJsonPathNumberValue("$.location.lat").isEqualTo(123.2134);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(100);
        assertThat(result).extractingJsonPathBooleanValue("$.requestModeration").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");
    }
}