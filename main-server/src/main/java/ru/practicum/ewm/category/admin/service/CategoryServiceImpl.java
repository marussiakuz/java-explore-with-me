package ru.practicum.ewm.category.admin.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.dto.CategoryChangedDto;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Override
    public CategoryOutDto createCategory(CategoryInDto categoryInDto) {
        return null;
    }

    @Override
    public CategoryOutDto updateCategory(CategoryChangedDto categoryChangedDto) {
        return null;
    }

    @Override
    public void deleteCategory(long catId) {

    }
}
