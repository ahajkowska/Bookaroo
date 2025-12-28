package org.example.bookaroo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Table(name="users")
@Entity
public class User implements UserDetails {

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
    private List<Review> givenReviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Bookshelf> bookshelves;

    // metody wymagane przez spring security (UserDetails)

    // Czy konto jest NIEzablokowane?
    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // mapowanie roli (String) na format Springa (GrantedAuthority)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}