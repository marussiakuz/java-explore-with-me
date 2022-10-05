package ru.practicum.ewm.category.shared.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    private static Category category;
    private static Category another;

    @BeforeAll
    public static void beforeAll() {
        category = Category.builder()
                .id(1L)
                .name("Concerts")
                .build();

        another = Category.builder()
                .id(2L)
                .name("Theater")
                .build();
    }

    @Test
    void whenGetCategoriesThenCallFindAll() {
        Page<Category> categories = new PageImpl<>(List.of(category, another));

        Mockito.when(categoryRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(categories);

        List<CategoryOutDto> returned = categoryService.getCategories(0, 10);

        assertThat(returned.size(), equalTo(2));
        assertThat(returned.get(0).getId(), equalTo(category.getId()));
        assertThat(returned.get(0).getName(), equalTo(category.getName()));
        assertThat(returned.get(1).getId(), equalTo(another.getId()));
        assertThat(returned.get(1).getName(), equalTo(another.getName()));

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findAll(Mockito.any(Pageable.class));
    }

    @Test
    void whenGetCategoryByIdIfItExistsThenCallGetCategoryByIdRepository() {
        Mockito.when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        CategoryOutDto categoryOut = categoryService.getCategoryById(2L);

        assertNotNull(categoryOut);
        assertThat(categoryOut.getId(), equalTo(category.getId()));
        assertThat(categoryOut.getName(), equalTo(category.getName()));

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(2L);
    }

    @Test
    void whenGetCategoryByIdIfItNotExistsThenThrowsCategoryNotFoundException() {
        Mockito.when(categoryRepository.findById(2L))
                .thenReturn(Optional.empty());

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(2L));

        Assertions.assertEquals("Category with id=2 not found", exception.getMessage());

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(2L);
    }
}