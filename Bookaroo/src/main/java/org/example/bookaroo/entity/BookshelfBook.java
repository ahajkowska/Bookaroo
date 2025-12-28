package org.example.bookaroo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bookshelf_books")
public class BookshelfBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bookshelf_id")
    private Bookshelf bookshelf;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    // Konstruktor pomocniczy
    public BookshelfBook(Bookshelf bookshelf, Book book) {
        this.bookshelf = bookshelf;
        this.book = book;
        this.addedAt = LocalDateTime.now();
    }
}