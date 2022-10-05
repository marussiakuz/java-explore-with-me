package ru.practicum.ewm.controller;

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
import ru.practicum.ewm.model.dto.ViewInDto;
import ru.practicum.ewm.model.dto.ViewOutDto;
import ru.practicum.ewm.service.StatService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatController.class)
@AutoConfigureMockMvc
class StatControllerTest {
    @Autowired
    private StatController statController;
    @MockBean
    private StatService statService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static ViewInDto view;
    private static ViewOutDto viewOut;

    @BeforeAll
    public static void beforeAll() {
        view = ViewInDto.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/1")
                .ip("0.0.0.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        viewOut = ViewOutDto.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/1")
                .hits(1)
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(statController)
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addViewStatusIsOk() throws Exception {
        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(view)))
                .andExpect(status().isOk());
    }

    @Test
    void getStatsStatusIsOk() throws Exception {
        String[] uris = {"http://localhost:8080/events/1"};
        Mockito
                .when(statService.getStats(null, null, uris, true))
                .thenReturn(List.of(viewOut));

        mockMvc.perform(get("/stats?uris=http://localhost:8080/events/1&unique=true")
                        .header("X-Ewm-Server-App", "ewm-main-server"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].app").value("ewm-main-server"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uri")
                        .value("http://localhost:8080/events/1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hits").value(1));
    }
}