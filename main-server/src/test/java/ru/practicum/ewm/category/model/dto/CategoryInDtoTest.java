package ru.practicum.ewm.category.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class CategoryInDtoTest {
    @Autowired
    JacksonTester<CategoryInDto> json;

    @Test
    void testCategoryInDto() throws IOException {
        CategoryInDto category = CategoryInDto.builder()
                .name("Theater")
                .build();

        JsonContent<CategoryInDto> result = json.write(category);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Theater");
    }
}