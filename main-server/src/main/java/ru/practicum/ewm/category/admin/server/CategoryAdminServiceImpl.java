package ru.practicum.ewm.category.admin.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.dto.CategoryChangedDto;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.category.model.mapper.CategoryMapper;
import ru.practicum.ewm.category.repository.CategoryRepository;

import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;

import ru.practicum.ewm.event.repository.EventRepository;

@Service
@Slf4j
public class CategoryAdminServiceImpl implements CategoryAdminService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CategoryAdminServiceImpl(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public CategoryOutDto createCategory(CategoryInDto categoryInDto) {
        Category category = categoryRepository.save(CategoryMapper.toCategory(categoryInDto));
        log.info("new category id={}, name={} successfully added", category.getId(), category.getName());
        return CategoryMapper.toCategoryOut(category);
    }

    @Override
    public CategoryOutDto updateCategory(CategoryChangedDto categoryChangedDto) {
        Category category = categoryRepository.findById(categoryChangedDto.getId())
                .orElseThrow(() -> new CategoryNotFoundException(String.format("Category with id=%s not found",
                        categoryChangedDto.getId())));

        category.setName(categoryChangedDto.getName());
        Category updated = categoryRepository.save(category);
        log.info("category id={}, name={} successfully updated", updated.getId(), updated.getName());

        return CategoryMapper.toCategoryOut(updated);
    }

    @Override
    public void deleteCategory(long catId) {
        if(eventRepository.existsByCategoryId(catId))
            throw new ConditionIsNotMetException("It isn't possible to delete a category due to binding at least one " +
                    "event to this category");

        if(!categoryRepository.existsById(catId))
            throw new CategoryNotFoundException(String.format("Category with id=%s not found", catId));

        categoryRepository.deleteById(catId);
        log.info("category id={} successfully deleted", catId);
    }
}
