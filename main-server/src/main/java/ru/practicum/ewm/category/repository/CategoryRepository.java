package ru.practicum.ewm.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.ewm.category.model.Category;

@EnableJpaRepositories
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

