package org.example.neighborly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table(name="users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="email", unique = true,  nullable=false)
    private String email;

    @Column(name="password", nullable=false)
    private String password;

    @Column(name="role", nullable=false)
    private String role;
}
