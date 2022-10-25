package ru.practicum.ewm.event.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class LocationDtoTest {
    @Autowired
    JacksonTester<LocationDto> json;

    @Test
    void testLocationDto() throws IOException {
        LocationDto location = LocationDto.builder()
                .latitude(46.4546f)
                .longitude(52.5483f)
                .build();

        JsonContent<LocationDto> result = json.write(location);

        assertThat(result).extractingJsonPathNumberValue("$.lon").isEqualTo(52.5483);
        assertThat(result).extractingJsonPathNumberValue("$.lat").isEqualTo(46.4546);
    }
}