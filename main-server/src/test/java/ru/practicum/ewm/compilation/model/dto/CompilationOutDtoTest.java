package ru.practicum.ewm.compilation.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.user.model.dto.UserShortOutDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class CompilationOutDtoTest {
    @Autowired
    JacksonTester<CompilationOutDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testCompilationOutDto() throws IOException {
        EventShortOutDto eventShortOut = EventShortOutDto.builder()
                .id(1L)
                .views(12)
                .paid(true)
                .annotation("Annotation")
                .eventDate(LocalDateTime.parse("2022-11-11 12:30:00", DATE_TIME_FORMATTER))
                .category(CategoryOutDto.builder()
                        .id(1L)
                        .name("Theater")
                        .build())
                .confirmedRequests(12)
                .initiator(UserShortOutDto.builder()
                        .id(5L)
                        .name("Initiator")
                        .build())
                .title("very interesting event")
                .build();

        CompilationOutDto compilation = CompilationOutDto.builder()
                .id(15)
                .title("Title")
                .pinned(false)
                .events(List.of(eventShortOut))
                .build();

        JsonContent<CompilationOutDto> result = json.write(compilation);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(15);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("Title");
        assertThat(result).extractingJsonPathBooleanValue("$.pinned").isEqualTo(false);
        assertThat(result).extractingJsonPathNumberValue("$.events.length()").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.events[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.events[0].views").isEqualTo(12);
        assertThat(result).extractingJsonPathBooleanValue("$.events[0].paid").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.events[0].annotation")
                .isEqualTo("Annotation");
        assertThat(result).extractingJsonPathStringValue("$.events[0].eventDate")
                .isEqualTo("2022-11-11 12:30:00");
        assertThat(result).extractingJsonPathNumberValue("$.events[0].category.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.events[0].category.name")
                .isEqualTo("Theater");
        assertThat(result).extractingJsonPathNumberValue("$.events[0].confirmedRequests").isEqualTo(12);
        assertThat(result).extractingJsonPathNumberValue("$.events[0].initiator.id")
                .isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.events[0].initiator.name")
                .isEqualTo("Initiator");
        assertThat(result).extractingJsonPathStringValue("$.events[0].title")
                .isEqualTo("very interesting event");
    }
}