package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.ewm.event.model.Comment;

import java.util.Optional;

@EnableJpaRepositories
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByEventIdAndClosedIsFalse(long eventId);

    boolean existsByEventIdAndClosedIsFalse(long eventId);
}
