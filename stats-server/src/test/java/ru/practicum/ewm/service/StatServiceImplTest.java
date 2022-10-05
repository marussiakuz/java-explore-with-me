package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.model.View;
import ru.practicum.ewm.model.ViewWithHits;
import ru.practicum.ewm.model.dto.ViewInDto;
import ru.practicum.ewm.model.dto.ViewOutDto;
import ru.practicum.ewm.repo.StatRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {
    private final static DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @InjectMocks
    private StatServiceImpl statService;
    @Mock
    private StatRepository statRepository;
    private static ViewInDto viewIn;
    private static View view;
    private static View returned;
    private static ViewWithHits viewWithHits;
    private static String start;
    private static String end;

    @BeforeAll
    public static void setUp() {
        LocalDateTime timestamp = LocalDateTime.now();

        viewIn = ViewInDto.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/1")
                .ip("0.0.0.0.0.1")
                .timestamp(timestamp)
                .build();

        view = View.builder()
                .app("ewm-main-server")
                .ip("0.0.0.0.0.1")
                .uri("http://localhost:8080/events/1")
                .timestamp(timestamp)
                .build();

        returned = View.builder()
                .id(1L)
                .app("ewm-main-server")
                .ip("0.0.0.0.0.1")
                .uri("http://localhost:8080/events/1")
                .timestamp(timestamp)
                .build();

        viewWithHits = ViewWithHits.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/1")
                .hits(11L)
                .build();

        start = LocalDateTime.now().minusDays(1).format(DATE_TIME);
        end = LocalDateTime.now().format(DATE_TIME);
    }

    @Test
    void whenSaveViewThenCallSaveRepository() {
        Mockito.when(statRepository.save(view))
                .thenReturn(returned);

        statService.saveView(viewIn);

        Mockito.verify(statRepository, Mockito.times(1))
                .save(view);
    }

    @Test
    void whenGetStatsThenCallGetViewWithHitsRepository() {
        String[] uris = {"http://localhost:8080/events/1"};

        Mockito.when(statRepository.getViewWithHits(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.anyString(), Mockito.anyBoolean()))
                .thenReturn(List.of(viewWithHits));

        List<ViewOutDto> returned = statService.getStats(start, end, uris, false);

        assertThat(returned.size(), equalTo(1));
        assertThat(returned.get(0).getApp(), equalTo("ewm-main-server"));
        assertThat(returned.get(0).getUri(), equalTo("http://localhost:8080/events/1"));
        assertThat(returned.get(0).getHits(), equalTo(11L));

        Mockito.verify(statRepository, Mockito.times(1))
                .getViewWithHits(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    void whenGetStatsIfUrisIsAbsentThenReturnEmptyList() {
        List<ViewOutDto> returned = statService.getStats(start, end, null, false);

        assertTrue(returned.isEmpty());

        Mockito.verify(statRepository, Mockito.never())
                .getViewWithHits(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.anyString(), Mockito.anyBoolean());
    }
}