package ru.practicum.ewm.event.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class EventShortOutDtoTest {
    @Autowired
    JacksonTester<EventShortOutDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testEventShortOutDto() throws IOException {
        EventShortOutDto event = EventShortOutDto.builder()
                .id(18L)
                .views(12)
                .paid(true)
                .annotation("Annotation")
                .eventDate(LocalDateTime.parse("2022-11-11 12:30:00", DATE_TIME_FORMATTER))
                .category(CategoryOutDto.builder()
                        .id(13L)
                        .name("Theater")
                        .build())
                .confirmedRequests(12)
                .initiator(UserShortOutDto.builder()
                        .id(5L)
                        .name("Initiator")
                        .build())
                .title("very interesting event")
                .build();

        JsonContent<EventShortOutDto> result = json.write(event);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(18);
        assertThat(result).extractingJsonPathNumberValue("$.views").isEqualTo(12);
        assertThat(result).extractingJsonPathNumberValue("$.category.id").isEqualTo(13);
        assertThat(result).extractingJsonPathStringValue("$.category.name").isEqualTo("Theater");
        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("Annotation");
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo("2022-11-11 12:30:00");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.confirmedRequests").isEqualTo(12);
        assertThat(result).extractingJsonPathNumberValue("$.initiator.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.initiator.name").isEqualTo("Initiator");
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("very interesting event");
    }
}