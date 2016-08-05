package com.abo.todo.persistance;

import com.abo.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by joanna on 04.08.16.
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByUserLogin(String login);
}
