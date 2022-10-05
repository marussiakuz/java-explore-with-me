package ru.practicum.ewm.compilation.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class CompilationInDtoTest {
    @Autowired
    JacksonTester<CompilationInDto> json;

    @Test
    void testCompilationInDto() throws IOException {
        CompilationInDto compilation = CompilationInDto.builder()
                .events(new long[] {1, 25, 300})
                .pinned(true)
                .title("Title")
                .build();

        JsonContent<CompilationInDto> result = json.write(compilation);

        assertThat(result).extractingJsonPathNumberValue("$.events.length()").isEqualTo(3);
        assertThat(result).extractingJsonPathNumberValue("$.events[0]").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.events[1]]").isEqualTo(25);
        assertThat(result).extractingJsonPathNumberValue("$.events[2]").isEqualTo(300);
        assertThat(result).extractingJsonPathBooleanValue("$.pinned").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("Title");
    }
}