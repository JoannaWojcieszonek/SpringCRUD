package com.abo.todo.service;

import com.abo.todo.domain.Todo;
import com.abo.todo.web.TodoController;
import com.abo.user.web.UserController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by joanna on 04.08.16.
 */
@Service
public class TodoResourceAssembler extends ResourceAssemblerSupport<Todo, Resource>{
    public TodoResourceAssembler() {
        super(TodoController.class, Resource.class);
    }

    @Override
    public Resource toResource(Todo todo) {
        String login = todo.getUser().getLogin();
        return new Resource(todo,
                linkTo(TodoController.class).slash(login+"/todos/"+ todo.getId()).withSelfRel(),
                linkTo(UserController.class).slash(login).withRel("owner"));
    }
}
