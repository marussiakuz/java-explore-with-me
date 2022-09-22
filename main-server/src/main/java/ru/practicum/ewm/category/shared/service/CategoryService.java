package ru.practicum.ewm.category.shared.service;

import ru.practicum.ewm.category.model.dto.CategoryOutDto;

import java.util.List;

public interface CategoryService {
    List<CategoryOutDto> getCategories(int from, int size);
    CategoryOutDto getCategoryById(long catId);
}
