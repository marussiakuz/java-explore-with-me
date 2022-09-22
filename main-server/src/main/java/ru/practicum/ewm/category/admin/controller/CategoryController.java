package ru.practicum.ewm.category.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.admin.service.CategoryService;
import ru.practicum.ewm.category.model.dto.CategoryChangedDto;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public CategoryOutDto addCategory(@RequestBody CategoryInDto categoryInDto) {
        return categoryService.createCategory(categoryInDto);
    }

    @PatchMapping
    public CategoryOutDto addCategory(@RequestBody CategoryChangedDto categoryChangedDto) {
        return categoryService.updateCategory(categoryChangedDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable @Positive long catId) {
        categoryService.deleteCategory(catId);
    }
}
