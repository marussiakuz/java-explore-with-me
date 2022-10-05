package ru.practicum.ewm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.model.View;

@EnableJpaRepositories
public interface StatRepository extends JpaRepository<View, Long>, StatRepositoryCustom, QuerydslPredicateExecutor<View> {
}
