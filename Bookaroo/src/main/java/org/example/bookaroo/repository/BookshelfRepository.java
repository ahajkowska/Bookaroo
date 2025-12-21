package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Bookshelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, UUID> {

}