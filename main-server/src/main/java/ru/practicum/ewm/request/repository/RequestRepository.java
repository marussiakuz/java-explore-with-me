package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.event.enums.Status;
import ru.practicum.ewm.request.model.Request;

import java.util.List;

@EnableJpaRepositories
public interface RequestRepository extends JpaRepository<Request, Long>, QuerydslPredicateExecutor<Request> {

    long countByEventId(long eventId);

    long countByEventIdAndStatus(long eventId, Status status);

    List<Request> findAllByEventId(long eventId);

    List<Request> findAllByRequesterId(long userId);

    boolean existsByRequesterIdAndEventId(long userId, long eventId);
}
