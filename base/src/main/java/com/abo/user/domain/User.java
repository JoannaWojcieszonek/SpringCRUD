package com.abo.user.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

//TODO show AllArgsConstructor, NoArgsConstructor, Builder, Getter, ToString, EqualsAndHashCode

@Entity(name = "_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class User implements Serializable {

    public enum Role {
        USER, ADMIN
    }

    //TODO @NonNull
    private @Id @NonNull String login;
    private @Column(nullable = false) String passwordHash;
    private @Column(nullable = false) @Enumerated(EnumType.STRING) Role role;

}
