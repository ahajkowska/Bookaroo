package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, UUID> {

    List<Bookshelf> findAllByUserId(UUID userId);

    Optional<Bookshelf> findByUserAndName(User user, String shelfName);
}