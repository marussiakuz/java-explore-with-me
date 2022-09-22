package ru.practicum.ewm.category.shared.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Override
    public List<CategoryOutDto> getCategories(int from, int size) {
        return null;
    }

    @Override
    public CategoryOutDto getCategoryById(long catId) {
        return null;
    }
}
