package ru.practicum.ewm.category.shared.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.ewm.category.model.dto.CategoryOutDto;
import ru.practicum.ewm.category.shared.service.CategoryService;
import ru.practicum.ewm.error.handler.ErrorHandler;
import ru.practicum.ewm.error.handler.exception.CategoryNotFoundException;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    @Autowired
    private CategoryController categoryController;
    @MockBean
    private CategoryService categoryService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static CategoryOutDto categoryOut;

    @BeforeAll
    public static void beforeAll() {
        categoryOut = CategoryOutDto.builder()
                .id(1L)
                .name("Theater")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getCategoriesStatusIsOk() throws Exception {
        Mockito
                .when(categoryService.getCategories(0, 10))
                .thenReturn(List.of(categoryOut));

        mockMvc.perform(get("/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("Theater"));
    }

    @Test
    void getCategoriesIfFromParamIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/categories?from=-10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The from must be greater than or equal to 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }


    @ParameterizedTest
    @ValueSource(ints = { 0, -100 })
    void getCategoriesIfSizeIsZeroOrNegativeThenStatusIsBadRequest(int value) throws Exception {
        mockMvc.perform(get("/categories?from=10&size=" + value)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The min allowed value for the size is 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }

    @Test
    void getCategoryIsOk() throws Exception {
        Mockito
                .when(categoryService.getCategoryById(15))
                .thenReturn(categoryOut);

        mockMvc.perform(get("/categories/15")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Theater"));
    }

    @Test
    void getCategoryIfThrowsNotFoundExceptionThenStatusNotFound() throws Exception {
        Mockito
                .when(categoryService.getCategoryById(15))
                .thenThrow(new CategoryNotFoundException("Category with id=15 not found"));

        mockMvc.perform(get("/categories/15")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CategoryNotFoundException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("Category with id=15 not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("The required object was not found."))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("NOT_FOUND"));
    }

    @Test
    void getCategoryIfCategoryIdIsNegativeThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/categories/-15")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The value must be greater than 0"))
                .andExpect(MockMvcResultMatchers.jsonPath("reason")
                        .value("Error in URI parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("status")
                        .value("BAD_REQUEST"));
    }
}