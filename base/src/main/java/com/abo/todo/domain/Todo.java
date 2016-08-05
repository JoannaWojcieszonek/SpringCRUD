package com.abo.todo.domain;

import com.abo.user.domain.User;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by joanna on 04.08.16.
 */
@Entity(name = "_todo")
@Getter
@Setter
@NoArgsConstructor
public class Todo implements Serializable{
    public Todo(String content, User user) {
        this.content = content;
        this.user = user;
    }

    @Id @GeneratedValue
    private Long id;
    private String content;
    @ManyToOne
    private User user;
}
