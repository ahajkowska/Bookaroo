package org.example.bookaroo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Table(name="users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="username", unique = true, nullable=false)
    private String username;

    @Column(name="email", unique = true,  nullable=false)
    private String email;

    @Column(name="password", nullable=false)
    private String password;

    @Column(name="avatar")
    private String avatar;

    @Column(name="bio")
    private String bio;

    @Column(name="role", nullable=false)
    private String role;

    // pole do blokowania (domyślnie niezablokowany)
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reading_challenge_target")
    private Integer readingChallengeTarget;

    // Relacje
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> givenReviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Bookshelf> bookshelves = new ArrayList<>();

}