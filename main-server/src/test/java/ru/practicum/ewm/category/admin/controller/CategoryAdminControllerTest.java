package ru.practicum.ewm.category.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.ewm.category.admin.server.CategoryAdminService;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.dto.CategoryChangedDto;
import ru.practicum.ewm.category.model.dto.CategoryInDto;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;
import ru.practicum.ewm.error.handler.exception.ConditionIsNotMetException;
import ru.practicum.ewm.util.TextProcessing;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryAdminController.class)
@AutoConfigureMockMvc
class CategoryAdminControllerTest implements TextProcessing {
    @Autowired
    private CategoryAdminController categoryAdminController;
    @MockBean
    private CategoryAdminService categoryAdminService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static CategoryInDto categoryIn;
    private static CategoryOutDto categoryOut;
    private static CategoryChangedDto changed;

    @BeforeAll
    public static void beforeAll() {
        categoryIn = CategoryInDto.builder()
                .name("Theater")
                .build();

        categoryOut = CategoryOutDto.builder()
                .id(1L)
                .name("Theater")
                .build();

        changed = CategoryChangedDto.builder()
                .id(1L)
                .name("Concerts")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryAdminController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addCategoryStatusIsOk() throws Exception {
        Mockito
                .when(categoryAdminService.createCategory(categoryIn))
                .thenReturn(categoryOut);

        mockMvc.perform(post("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(categoryIn)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Theater"));
    }

    @Test
    void addCategoryIfNameIsNullThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CategoryInDto())))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The name of the category must not be blank"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void addCategoryIfNameTooLongThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Category.builder()
                                .name(createText(65)).build())))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The name of the category must be between 1 and 64 characters long"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void updateCategoryStatusIsOk() throws Exception {
        Mockito
                .when(categoryAdminService.updateCategory(changed))
                .thenReturn(categoryOut);

        mockMvc.perform(patch("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(changed)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Theater"));
    }

    @Test
    void updateCategoryIfThrowsCategoryNotFoundExceptionThenStatusNotFound() throws Exception {
        Mockito
                .when(categoryAdminService.updateCategory(changed))
                .thenThrow(new CategoryNotFoundException("Category with id=1 not found"));

        mockMvc.perform(patch("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(changed)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CategoryNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Category with id=1 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void updateCategoryIfNameIsNullThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CategoryChangedDto())))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The name of the category must not be blank"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void updateCategoryIfNameTooLongThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Category.builder()
                                .name(createText(65)).build())))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The name of the category must be between 1 and 64 characters long"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Field error in object"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void deleteCategoryStatusIsOk() throws Exception {
        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCategoryIfCategoryIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/categories/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void deleteCategoryIfThrowsCategoryNotFoundExceptionThenStatusIsNotFound() throws Exception {
        Mockito
                .doThrow(new CategoryNotFoundException("Category with id=1 not found"))
                .when(categoryAdminService).deleteCategory(1);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CategoryNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Category with id=1 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void deleteCategoryIfThrowsConditionIsNotMetExceptionThenStatusIsConflict() throws Exception {
        Mockito
                .doThrow(new ConditionIsNotMetException("It isn't possible to delete a category due to binding at " +
                        "least one event to this category"))
                .when(categoryAdminService).deleteCategory(1);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConditionIsNotMetException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("It isn't possible to delete a category due to binding at least one event " +
                                "to this category"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("For the requested operation the conditions are not met."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("CONFLICT"));
    }
}