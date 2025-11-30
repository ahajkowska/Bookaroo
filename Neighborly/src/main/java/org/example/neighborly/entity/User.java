package org.example.neighborly.entity;

import jakarta.persistence.*;
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

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="email", unique = true,  nullable=false)
    private String email;

    @Column(name="password", nullable=false)
    private String password;

    @Column(name="role", nullable=false)
    private String role;

    @Column(name="reputation_score")
    private Integer reputationScore = 0;

    // Relacje
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<ServiceOffer> offeredServices;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    private List<ServiceRequest> requestedServices;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL)
    private List<Review> givenReviews;

    @OneToMany(mappedBy = "reviewedUser", cascade = CascadeType.ALL)
    private List<Review> receivedReviews;
}
