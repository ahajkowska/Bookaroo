package org.example.bookaroo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.bookaroo.dto.UserBackupDTO;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.Review;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.BookshelfRepository;
import org.example.bookaroo.repository.ReviewRepository;
import org.example.bookaroo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;

@Service
public class BackupService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookshelfRepository bookshelfRepository;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper;

    private static final String UPLOAD_DIR = "backups/";

    public BackupService(UserRepository userRepository, BookRepository bookRepository,
                         BookshelfRepository bookshelfRepository, ReviewRepository reviewRepository,
                         ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookshelfRepository = bookshelfRepository;
        this.reviewRepository = reviewRepository;
        this.objectMapper = objectMapper;

        // czy na pewno katalog istnieje
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Nie można utworzyć katalogu na backupy", e);
        }
    }

    // EKSPORT DANYCH (JSON)
    @Transactional(readOnly = true)
    public byte[] exportUserDataToJson(String username) throws IOException {
        UserBackupDTO backupData = gatherUserData(username);
        // Konwersja obiektu Java na bajty JSON
        return objectMapper.writeValueAsBytes(backupData);
    }

    // EKSPORT DANYCH (CSV)
    public byte[] exportUserReviewsToCsv(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika o loginie: " + username));
        try (StringWriter sw = new StringWriter()) {

            sw.write('\uFEFF'); // polski

            // separator na średnik (';')
            try (CSVWriter csvWriter = new CSVWriter(sw,
                    ';',                             // Separator kolumn
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,        // cudzysłów
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,       // Znak ucieczki
                    CSVWriter.DEFAULT_LINE_END)) {            // Koniec linii

                // Nagłówki
                csvWriter.writeNext(new String[]{"Tytuł książki", "Autor", "ISBN", "Ocena", "Treść recenzji"});

                // Dane
                for (var review : user.getGivenReviews()) {
                    csvWriter.writeNext(new String[]{
                            review.getBook().getTitle(),
                            review.getBook().getAuthor() != null ?
                                    review.getBook().getAuthor().getName() + " " + review.getBook().getAuthor().getSurname() : "Brak autora",
                            review.getBook().getIsbn(),
                            String.valueOf(review.getRating()),
                            review.getContent()
                    });
                }
            }

            return sw.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas generowania CSV", e);
        }
    }

    public byte[] exportUserReviewsToPdf(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika o loginie: " + username));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            // Tytuł
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Recenzje uzytkownika: " + username, fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Tabela
            PdfPTable table = new PdfPTable(3); // 3 kolumny
            table.setWidthPercentage(100);
            table.addCell("Tytul ksiazki");
            table.addCell("Ocena");
            table.addCell("Tresc");

            for (var review : user.getGivenReviews()) {
                table.addCell(review.getBook().getTitle());
                table.addCell(String.valueOf(review.getRating()));
                table.addCell(review.getContent());
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania PDF", e);
        }
    }

    // m. pomocnicza zbierająca dane
    private UserBackupDTO gatherUserData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika: " + username));

        var shelfDtos = user.getBookshelves().stream().map(
                shelf -> new org.example.bookaroo.dto.ShelfBackupDTO(
                shelf.getName(),
                shelf.getBooks().stream().map(Book::getIsbn).toList()
        )).toList();

        var reviewDtos = user.getGivenReviews().stream().map(
                review -> new org.example.bookaroo.dto.ReviewBackupDTO(
                review.getBook().getIsbn(),
                review.getContent(),
                review.getRating()
        )).toList();

        return new UserBackupDTO(shelfDtos, reviewDtos);
    }

    // IMPORT DANYCH
    @Transactional
    public void importUserData(String username, MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        UserBackupDTO backupDto = objectMapper.readValue(file.getInputStream(), UserBackupDTO.class);

        User user = userRepository.findByUsername(username).orElseThrow();

        // Import Półek
        if (backupDto.shelves() != null) {
            for (var shelfDto : backupDto.shelves()) {
                Bookshelf shelf = user.getBookshelves().stream()
                        .filter(s -> s.getName().equalsIgnoreCase(shelfDto.name()))
                        .findFirst()
                        .orElseGet(() -> {
                            Bookshelf newShelf = new Bookshelf();
                            newShelf.setName(shelfDto.name());
                            newShelf.setUser(user);
                            newShelf.setIsDefault(false);
                            return bookshelfRepository.save(newShelf);
                        });

                for (String isbn : shelfDto.bookIsbns()) {
                    bookRepository.findByIsbn(isbn).ifPresent(book -> {
                        boolean alreadyOnShelf = shelf.getBooks().stream()
                                .anyMatch(b -> b.getId().equals(book.getId()));

                        if (!alreadyOnShelf) {
                            shelf.addBook(book);
                        }
                    });
                }
                bookshelfRepository.save(shelf);
            }
        }

        // Import Recenzji
        if (backupDto.reviews() != null) {
            for (var reviewDto : backupDto.reviews()) {
                bookRepository.findByIsbn(reviewDto.bookIsbn()).ifPresent(book -> {
                    boolean reviewExists = user.getGivenReviews().stream()
                            .anyMatch(r -> r.getBook().getId().equals(book.getId()));

                    if (!reviewExists) {
                        Review review = new Review();
                        review.setUser(user);
                        review.setBook(book);
                        review.setContent(reviewDto.content());
                        review.setRating(reviewDto.rating());
                        reviewRepository.save(review);
                    }
                });
            }
        }

        userRepository.save(user);
    }
}