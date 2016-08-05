package com.abo.todo.web;

import com.abo.security.LoggedUserGetter;
import com.abo.todo.domain.Todo;
import com.abo.user.domain.User;
import com.abo.todo.persistance.TodoRepository;
import com.abo.user.persistance.UserRepository;
import com.abo.todo.service.TodoResourceAssembler;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by joanna on 04.08.16.
 */
@RequestMapping("/users")
@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TodoController {

    private @NonNull
    TodoRepository todoRepository;
    private @NonNull
    TodoResourceAssembler todoResourceAssembler;
    private @NonNull
    UserRepository userRepository;
    private @NonNull
    LoggedUserGetter loggedUserGetter;

    @RequestMapping(value = "/{login}/todos",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List todos(@PathVariable String login) {
        return todoRepository.findAll().stream()
                .filter(a -> a.getUser().getLogin().equals(login))
                .map(todoResourceAssembler::toResource)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{login}/todos/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Resource<Todo> readTodo(@PathVariable String login, @PathVariable Long id) {
        return todoResourceAssembler.toResource(todoRepository.findOne(id));
    }

    @RequestMapping(value = "/{login}/todos/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Todo> deleteTodo(@PathVariable String login, @PathVariable Long id) {
        if(todoRepository.exists(id)) {
            Todo todo = todoRepository.findOne(id);
            todoRepository.delete(todo);
            return new ResponseEntity<Todo>(todo, HttpStatus.OK);
        }
        return new ResponseEntity<Todo>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{login}/todos", method = RequestMethod.DELETE)
    public void deleteTodos(@PathVariable String login) {
        todoRepository.findByUserLogin(login).forEach(todo -> todoRepository.delete(todo));
    }

    @RequestMapping(value = "/{login}/todos", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Resource<Todo> createTodo(@PathVariable String login, @RequestBody Todo todo) {
        User user = loggedUserGetter.getLoggedUser();
        todo.setUser(user);
        todoRepository.save(todo);
        return todoResourceAssembler.toResource(todo);
    }

    @RequestMapping(value = "/{login}/todos/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo newTodo) {
        User user = loggedUserGetter.getLoggedUser();
        Optional<Todo> todo = Optional.ofNullable(todoRepository.findOne(id));
        if(todo.isPresent()) {
            newTodo.setId(id);
            newTodo.setUser(user);
            todoRepository.save(newTodo);
            return new ResponseEntity<Todo>(newTodo, HttpStatus.OK);
        }
        return new ResponseEntity<Todo>(HttpStatus.NOT_FOUND);
    }
}
