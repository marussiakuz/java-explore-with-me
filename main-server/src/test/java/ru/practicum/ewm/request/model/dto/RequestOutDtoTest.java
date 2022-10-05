package ru.practicum.ewm.request.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.event.enums.Status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class RequestOutDtoTest {
    @Autowired
    JacksonTester<RequestOutDto> json;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testRequestOutDto() throws IOException {
        RequestOutDto request = RequestOutDto.builder()
                .id(5L)
                .event(1L)
                .status(Status.PENDING)
                .requester(2L)
                .created(LocalDateTime.parse("2022-09-30 12:30:00", DATE_TIME_FORMATTER))
                .build();

        JsonContent<RequestOutDto> result = json.write(request);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.event").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("PENDING");
        assertThat(result).extractingJsonPathNumberValue("$.requester").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2022-09-30 12:30:00");
    }
}