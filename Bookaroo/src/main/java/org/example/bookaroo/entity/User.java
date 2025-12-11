package org.example.bookaroo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.Getter;
import lombok.Setter;

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

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relacje

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL)
    private List<Review> givenReviews;

    @OneToMany(mappedBy = "reviewedUser", cascade = CascadeType.ALL)
    private List<Review> receivedReviews;
}
