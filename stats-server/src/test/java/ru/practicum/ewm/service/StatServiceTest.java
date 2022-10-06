package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.View;
import ru.practicum.ewm.model.dto.ViewInDto;
import ru.practicum.ewm.model.dto.ViewOutDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatServiceTest {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EntityManager em;
    private final StatService statService;

    @ParameterizedTest
    @CsvSource({"ewm-main-server, http://localhost:8080/events/1, 0.0.0.0.0.1, 2022-12-12 13:13:13",
            "ewm-another-server, http://localhost:7070/another/5, 0.0.0.0.0.9, 2022-09-09 15:30:49"})
    void saveView(String app, String uri, String ip, String timestamp) {
        ViewInDto viewIn = ViewInDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.parse(timestamp, DATE_TIME))
                .build();

        statService.saveView(viewIn);

        TypedQuery<View> query = em.createQuery("Select v from View v where v.app = :app and v.ip = :ip " +
                "and v.uri = : uri and v.timestamp = :timestamp", View.class);
        View view = query
                .setParameter("app", viewIn.getApp())
                .setParameter("ip", viewIn.getIp())
                .setParameter("uri", viewIn.getUri())
                .setParameter("timestamp", viewIn.getTimestamp())
                .getSingleResult();

        assertThat(view, notNullValue());
    }

    @ParameterizedTest
    @CsvSource({"ewm-main-server, http://localhost:8080/events/2, 0.0.0.0.1.1, 1",
            "ewm-another-server, http://localhost:7070/another/15, 0.0.0.0.0.9, 5",
            "more-views-server, http://localhost:6060/another/2, 0.0.0.0.0.9, 100"})
    void getStatsIfUniqueFalse(String app, String uri, String ip, long hits) {
        ViewInDto viewIn = ViewInDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        LongStream.range(0, hits).forEach(iteration -> statService.saveView(viewIn));

        String[] uris = {uri};
        List<ViewOutDto> found = statService.getStats(null, null, uris, false);

        TypedQuery<View> query = em.createQuery("Select v from View v where v.app = :app and v.uri = :uri",
                View.class);
        List<View> views = query
                .setParameter("app", app)
                .setParameter("uri", uri)
                .getResultList();

        assertThat(found.size(), equalTo(1));
        assertThat((long) views.size(), equalTo(hits));
        assertThat(found.get(0).getHits(), equalTo(hits));
        assertThat(found.get(0).getUri(), equalTo(uri));
        assertThat(found.get(0).getApp(), equalTo(app));
    }

    @ParameterizedTest
    @CsvSource({"ewm-another-server, http://localhost:7070/another/15, 0.0.0.0.0.9, 5",
            "more-views-server, http://localhost:6060/another/2, 0.0.0.0.0.9, 100"})
    void getStatsIfUniqueTrue(String app, String uri, String ip, long hits) {
        ViewInDto viewIn = ViewInDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        LongStream.range(0, hits).forEach(iteration -> statService.saveView(viewIn));

        String[] uris = {uri};
        List<ViewOutDto> found = statService.getStats(null, null, uris, true);

        TypedQuery<View> query = em.createQuery("Select v from View v where v.app = :app and v.uri = :uri",
                View.class);
        List<View> views = query
                .setParameter("app", app)
                .setParameter("uri", uri)
                .getResultList();

        assertThat(found.size(), equalTo(1));
        assertThat((long) views.size(), equalTo(hits));
        assertThat(found.get(0).getHits(), equalTo(1L));
        assertThat(found.get(0).getUri(), equalTo(uri));
        assertThat(found.get(0).getApp(), equalTo(app));
    }
}