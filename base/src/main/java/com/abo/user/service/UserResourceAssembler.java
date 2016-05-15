package com.abo.user.service;

import com.abo.user.domain.User;
import com.abo.user.web.UserController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
public class UserResourceAssembler extends ResourceAssemblerSupport<User, Resource> {

    public UserResourceAssembler() {
        super(UserController.class, Resource.class);
    }

    @Override
    public List<Resource> toResources(Iterable<? extends User> users) {
        return StreamSupport.stream(users.spliterator(), false)
                .map(user -> new Resource(user, linkTo(UserController.class).slash(user.getLogin()).withSelfRel()))
                .collect(Collectors.toList());
    }

    @Override
    public Resource<User> toResource(User user) {
        return new Resource(user, linkTo(UserController.class).slash(user.getLogin()).withSelfRel());
    }

}
