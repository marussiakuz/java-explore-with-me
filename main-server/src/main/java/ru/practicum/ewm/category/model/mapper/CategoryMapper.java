package ru.practicum.ewm.category.model.mapper;

import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;

public class CategoryMapper {

    public static CategoryOutDto toCategoryOut(Category category) {
        return CategoryOutDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toCategory(CategoryInDto category) {
        return Category.builder()
                .name(category.getName())
                .build();
    }
}
