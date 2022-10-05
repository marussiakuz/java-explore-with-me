package ru.practicum.ewm.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.user.model.User;

public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User>  {

}
