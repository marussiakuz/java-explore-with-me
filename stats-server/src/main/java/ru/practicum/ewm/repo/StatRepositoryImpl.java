package ru.practicum.ewm.repo;

import com.querydsl.core.ResultTransformer;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.model.ViewWithHits;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.ewm.model.QView.view;

@Slf4j
@RequiredArgsConstructor
public class StatRepositoryImpl implements StatRepositoryCustom {
    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<ViewWithHits> getViewWithHits(LocalDateTime start, LocalDateTime end, String uri, boolean unique) {
        return buildQuery(start, end, uri)
                .transform(buildDtoTransformer(unique));
    }

    private JPAQuery<Tuple> buildQuery(LocalDateTime start, LocalDateTime end, String uri) {
        return new JPAQuery<>(em)
                .select(view.app, view.uri, view.ip)
                .from(view)
                .where(getFinalCondition(start, end, uri))
                .groupBy(view.uri, view.app);
    }

    private ResultTransformer<List<ViewWithHits>> buildDtoTransformer(boolean unique) {
        return GroupBy
                .groupBy(view.uri, view.app)
                .list(Projections.constructor(ViewWithHits.class, view.app, view.uri, unique ?
                        view.ip.countDistinct().as("hits") : view.ip.count().as("hits")));
    }

    private BooleanExpression getFinalCondition(LocalDateTime start, LocalDateTime end, String uri) {
        List<BooleanExpression> conditions = new ArrayList<>();

        conditions.add(view.uri.eq(uri));
        if (start != null) conditions.add(view.timestamp.after(start));
        if (end != null) conditions.add(view.timestamp.before(end));

        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();

        log.debug("the final condition has been formed: {}", finalCondition);

        return finalCondition;
    }
}
