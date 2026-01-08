package org.example.bookaroo.dto.mapper;

import org.example.bookaroo.dto.ReviewDTO;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Review;
import org.example.bookaroo.entity.User;

public class ReviewMapper {

    // ENCJA -> DTO (do wyświetlania)
    public static ReviewDTO toDto(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getUser().getAvatar(),
                review.getBook().getId()
        );
    }

    // DTO -> ENCJA (do zapisywania)
    public static Review toEntity(ReviewDTO dto, User user, Book book) {
        Review review = new Review();
        review.setRating(dto.rating());
        review.setContent(dto.content());
        review.setUser(user);
        review.setBook(book);
        return review;
    }
}