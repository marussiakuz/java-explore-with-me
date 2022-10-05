package ru.practicum.ewm.repo;

import ru.practicum.ewm.model.ViewWithHits;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepositoryCustom {

    List<ViewWithHits> getViewWithHits(LocalDateTime start, LocalDateTime end, String uri, boolean unique);
}
