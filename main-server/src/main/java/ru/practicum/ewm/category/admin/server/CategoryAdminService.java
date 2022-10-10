package ru.practicum.ewm.category.admin.server;

import ru.practicum.ewm.category.model.dto.CategoryChangedDto;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;

public interface CategoryAdminService {

    CategoryOutDto createCategory(CategoryInDto categoryInDto);

    CategoryOutDto updateCategory(CategoryChangedDto categoryChangedDto);

    void deleteCategory(long catId);
}
