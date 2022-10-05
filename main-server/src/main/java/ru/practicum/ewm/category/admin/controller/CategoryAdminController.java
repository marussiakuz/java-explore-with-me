package ru.practicum.ewm.category.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.dto.CategoryChangedDto;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/categories")
@Validated
public class CategoryAdminController {
    private final CategoryAdminService categoryAdminService;

    @Autowired
    public CategoryAdminController(CategoryAdminService categoryAdminService) {
        this.categoryAdminService = categoryAdminService;
    }

    @PostMapping
    public CategoryOutDto addCategory(@RequestBody @Valid CategoryInDto categoryInDto) {
        return categoryAdminService.createCategory(categoryInDto);
    }

    @PatchMapping
    public CategoryOutDto updateCategory(@RequestBody @Valid CategoryChangedDto categoryChangedDto) {
        return categoryAdminService.updateCategory(categoryChangedDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable @Positive(message = "The value must be greater than 0") long catId) {
        categoryAdminService.deleteCategory(catId);
    }
}
