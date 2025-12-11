package org.example.bookaroo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Table(name = "bookshelf")
@Entity
public class Bookshelf {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String name; // np. Przecztane

    @Column(name="is_default", nullable=false)
    private Boolean isDefault = false; // czy to domyślna półka

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // właściciel

    @ManyToMany
    @JoinTable(
            name = "bookshelf_books",
            joinColumns = @JoinColumn(name = "bookshelf_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> books;
}
