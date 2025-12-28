package org.example.bookaroo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
    @OneToMany(mappedBy = "bookshelf", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookshelfBook> items = new ArrayList<>();

    // zwraca listę książek
    public List<Book> getBooks() {
        return items.stream()
                .map(BookshelfBook::getBook)
                .collect(Collectors.toList());
    }

    // metoda do dodawania (potrzebna np. przy imporcie)
    public void addBook(Book book) {
        BookshelfBook item = new BookshelfBook(this, book);
        items.add(item);
    }
}
