package ru.practicum.ewm.category.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.category.model.mapper.CategoryMapper;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;
import ru.practicum.ewm.util.Pagination;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryOutDto> getCategories(int from, int size) {
        return categoryRepository.findAll(Pagination.of(from, size)).getContent().stream()
                .map(CategoryMapper::toCategoryOut)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryOutDto getCategoryById(long catId) {
        return CategoryMapper.toCategoryOut(categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(String.format("Category with id=%s not found", catId))));
    }
}
