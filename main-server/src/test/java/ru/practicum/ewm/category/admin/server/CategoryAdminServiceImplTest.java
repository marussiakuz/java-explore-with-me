package ru.practicum.ewm.category.admin.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.dto.CategoryChangedDto;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.event.repository.EventRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CategoryAdminServiceImplTest {
    @InjectMocks
    private CategoryAdminServiceImpl categoryAdminService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRepository eventRepository;

    private static CategoryInDto categoryIn;
    private static CategoryChangedDto changed;
    private static Category category;

    @BeforeAll
    public static void beforeAll() {
        categoryIn = CategoryInDto.builder()
                .name("Theater")
                .build();

        changed = CategoryChangedDto.builder()
                .id(5L)
                .name("Concerts")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Concerts")
                .build();
    }

    @Test
    void whenCreateCategoryThenCallSaveRepository() {
        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(category);

        CategoryOutDto returned = categoryAdminService.createCategory(categoryIn);

        assertNotNull(returned);
        assertThat(returned.getId(), equalTo(category.getId()));
        assertThat(returned.getName(), equalTo(category.getName()));

        Mockito.verify(categoryRepository, Mockito.times(1))
                .save(Mockito.any(Category.class));
    }

    @Test
    void whenUpdateCategoryIfCategoryExistsThenCallSaveRepository() {
        Mockito.when(categoryRepository.findById(5L))
                .thenReturn(Optional.of(category));

        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(category);

        CategoryOutDto returned = categoryAdminService.updateCategory(changed);

        assertNotNull(returned);
        assertThat(returned.getId(), equalTo(category.getId()));
        assertThat(returned.getName(), equalTo(category.getName()));

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .save(Mockito.any(Category.class));
    }

    @Test
    void whenUpdateCategoryIfCategoryNotExistsThenThrowsCategoryNotFoundException() {
        Mockito.when(categoryRepository.findById(5L))
                .thenReturn(Optional.empty());

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryAdminService.updateCategory(changed));

        Assertions.assertEquals("Category with id=5 not found", exception.getMessage());

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findById(5L);

        Mockito.verify(categoryRepository, Mockito.never())
                .save(Mockito.any(Category.class));
    }

    @Test
    void whenDeleteCategoryIfEventExistsWithThisCategoryThenThrowsConditionIsNotMetException() {
        Mockito.when(eventRepository.existsByCategoryId(5))
                .thenReturn(true);

        final ConditionIsNotMetException exception = Assertions.assertThrows(
                ConditionIsNotMetException.class,
                () -> categoryAdminService.deleteCategory(5));

        Assertions.assertEquals("It isn't possible to delete a category due to binding at least one event " +
                "to this category", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .existsByCategoryId(5);

        Mockito.verify(categoryRepository, Mockito.never())
                .existsById(Mockito.anyLong());

        Mockito.verify(categoryRepository, Mockito.never())
                .deleteById(Mockito.anyLong());
    }

    @Test
    void whenDeleteCategoryIfItNotExistsThenThrowsCategoryNotFoundException() {
        Mockito.when(eventRepository.existsByCategoryId(5))
                .thenReturn(false);

        Mockito.when(categoryRepository.existsById(5L))
                .thenReturn(false);

        final CategoryNotFoundException exception = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryAdminService.deleteCategory(5));

        Assertions.assertEquals("Category with id=5 not found", exception.getMessage());

        Mockito.verify(eventRepository, Mockito.times(1))
                .existsByCategoryId(5);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());

        Mockito.verify(categoryRepository, Mockito.never())
                .deleteById(Mockito.anyLong());
    }

    @Test
    void whenDeleteCategoryThenCallDeleteRepository() {
        Mockito.when(eventRepository.existsByCategoryId(5))
                .thenReturn(false);

        Mockito.when(categoryRepository.existsById(5L))
                .thenReturn(true);

        categoryAdminService.deleteCategory(5);

        Mockito.verify(eventRepository, Mockito.times(1))
                .existsByCategoryId(5);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .existsById(5L);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .deleteById(5L);
    }
}