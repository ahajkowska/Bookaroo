package org.example.bookaroo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Table(name="books")
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="isbn", unique = true,  nullable=false)
    private String isbn;

    @Column(name="description")
    private String description;

    @Column(name="publication_year")
    private Integer publicationYear;

    @Column(name="average_rating")
    private Double averageRating;

    @Column(name="total_reviews")
    private Integer totalReviews;

    // Relacje
    @ManyToOne
    @JoinColumn(name="author_id", nullable=false)
    private Author author;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Review> reviews;

}
