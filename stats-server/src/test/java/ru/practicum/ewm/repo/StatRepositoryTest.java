package ru.practicum.ewm.repo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.ewm.model.View;
import ru.practicum.ewm.model.ViewWithHits;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class StatRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private StatRepository statRepository;

    @Test
    void save() {
        LocalDateTime timestamp = LocalDateTime.now();

        View view = View.builder()
                .app("ewm-main-server")
                .ip("0.0.0.0.0.1")
                .uri("http://localhost:8080/events/1")
                .timestamp(timestamp)
                .build();

        em.persist(view);

        View addedView = statRepository.save(view);

        assertNotNull(addedView.getId());
        assertThat(addedView.getIp(), equalTo("0.0.0.0.0.1"));
        assertThat(addedView.getApp(), equalTo("ewm-main-server"));
        assertThat(addedView.getUri(), equalTo("http://localhost:8080/events/1"));
        assertThat(addedView.getTimestamp(), equalTo(timestamp));
    }

    @ParameterizedTest
    @CsvSource({"1, 15", "5, 20", "50, 100"})
    void getHitsByTime(long fitsTheTime, long totalViews) {
        LongStream.range(0, totalViews)
                .forEach(iteration -> {
                    View view = createAnotherView();
                    if(iteration >= fitsTheTime)
                        view.setTimestamp(LocalDateTime.now().minusHours(2).plusSeconds(iteration));
                    em.persist(view);
                });

        List<ViewWithHits> viewsByTime = statRepository.getViewWithHits(LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now(), "http://localhost:8080/events/10", false);

        assertThat(viewsByTime.size(), equalTo(1));
        assertThat(viewsByTime.get(0).getApp(), equalTo("ewm-main-server"));
        assertThat(viewsByTime.get(0).getUri(), equalTo("http://localhost:8080/events/10"));
        assertThat(viewsByTime.get(0).getHits(), equalTo(fitsTheTime));

        List<ViewWithHits> viewsWithoutTime = statRepository.getViewWithHits(null, null,
                "http://localhost:8080/events/10", false);

        assertThat(viewsWithoutTime.size(), equalTo(1));
        assertThat(viewsWithoutTime.get(0).getApp(), equalTo("ewm-main-server"));
        assertThat(viewsWithoutTime.get(0).getUri(), equalTo("http://localhost:8080/events/10"));
        assertThat(viewsWithoutTime.get(0).getHits(), equalTo(totalViews));
    }

    @ParameterizedTest
    @CsvSource({"1, 10", "5, 50", "55, 99"})
    void getHitsByUnique(long unique, long totalViews) {
        LongStream.range(0, totalViews)
                .forEach(iteration -> {
                    View view = createAnotherView();
                    if (iteration < unique) view.setIp("0.0.0.0.0.155." + iteration);
                    em.persist(view);
                });

        List<ViewWithHits> viewsUniqueTrue = statRepository.getViewWithHits(null, null,
                "http://localhost:8080/events/10", true);

        assertThat(viewsUniqueTrue.size(), equalTo(1));
        assertThat(viewsUniqueTrue.get(0).getApp(), equalTo("ewm-main-server"));
        assertThat(viewsUniqueTrue.get(0).getUri(), equalTo("http://localhost:8080/events/10"));
        assertThat(viewsUniqueTrue.get(0).getHits(), equalTo(unique + 1));

        List<ViewWithHits> viewsUniqueFalse = statRepository.getViewWithHits(null, null,
                "http://localhost:8080/events/10", false);

        assertThat(viewsUniqueFalse.size(), equalTo(1));
        assertThat(viewsUniqueFalse.get(0).getApp(), equalTo("ewm-main-server"));
        assertThat(viewsUniqueFalse.get(0).getUri(), equalTo("http://localhost:8080/events/10"));
        assertThat(viewsUniqueFalse.get(0).getHits(), equalTo(totalViews));
    }

    private View createAnotherView() {
        return View.builder()
                .app("ewm-main-server")
                .uri("http://localhost:8080/events/10")
                .timestamp(LocalDateTime.now())
                .ip("0.0.0.0.127.1")
                .build();
    }
}