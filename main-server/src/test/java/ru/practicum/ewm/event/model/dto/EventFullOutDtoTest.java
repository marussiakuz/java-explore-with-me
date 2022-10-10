package ru.practicum.ewm.event.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class EventFullOutDtoTest {
    @Autowired
    JacksonTester<EventFullOutDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testEventFullOutDto() throws IOException {
        EventFullOutDto event = EventFullOutDto.builder()
                .id(1L)
                .views(12)
                .paid(true)
                .state(State.PENDING)
                .requestModeration(true)
                .participantLimit(20)
                .annotation("Annotation")
                .description("description")
                .location(LocationDto.builder()
                        .latitude(46.4546f)
                        .longitude(52.5483f)
                        .build())
                .createdOn(LocalDateTime.parse("2022-09-29 15:46:17", DATE_TIME_FORMATTER))
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

        JsonContent<EventFullOutDto> result = json.write(event);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.views").isEqualTo(12);
        assertThat(result).extractingJsonPathStringValue("$.state").isEqualTo("PENDING");
        assertThat(result).extractingJsonPathNumberValue("$.category.id").isEqualTo(13);
        assertThat(result).extractingJsonPathStringValue("$.category.name").isEqualTo("Theater");
        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("Annotation");
        assertThat(result).extractingJsonPathStringValue("$.createdOn")
                .isEqualTo("2022-09-29 15:46:17");
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo("2022-11-11 12:30:00");
        assertThat(result).extractingJsonPathNumberValue("$.location.longitude").isEqualTo(52.5483);
        assertThat(result).extractingJsonPathNumberValue("$.location.latitude").isEqualTo(46.4546);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(20);
        assertThat(result).extractingJsonPathNumberValue("$.confirmedRequests").isEqualTo(12);
        assertThat(result).extractingJsonPathNumberValue("$.initiator.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.initiator.name").isEqualTo("Initiator");
        assertThat(result).extractingJsonPathBooleanValue("$.requestModeration").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("very interesting event");
    }
}