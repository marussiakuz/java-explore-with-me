package ru.practicum.ewm.category.shared.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.category.shared.service.CategoryService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryOutDto> getCategories(@RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryOutDto getCategory(@PathVariable @Positive long catId) {
        return categoryService.getCategoryById(catId);
    }

}
