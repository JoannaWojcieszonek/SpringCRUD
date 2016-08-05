package com.abo.user.service;

import com.abo.todo.domain.Todo;
import com.abo.user.domain.User;
import com.abo.todo.persistance.TodoRepository;
import com.abo.user.persistance.UserRepository;
import javaslang.collection.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserInitializer {

    @Autowired
    public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, TodoRepository todoRepository) {
        Stream.of(
                User.builder().login("james_bond").passwordHash(passwordEncoder.encode("james_bond")).role(User.Role.ADMIN).build()
                , User.builder().login("harry_potter").passwordHash(passwordEncoder.encode("harry_potter")).role(User.Role.USER).build()
        ).forEach(user -> {
            userRepository.save(user);
            log.debug("user added: {}", user);
        });
        Stream.of(
                new Todo("asdada", userRepository.findOne("james_bond")),
                new Todo("bbbbbbbbbb", userRepository.findOne("james_bond")),
                new Todo("aaaaa", userRepository.findOne("harry_potter"))
        ).forEach(todo -> todoRepository.save(todo));
    }

}
