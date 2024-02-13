package org.rnott.example.persistence;

import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends AbstractEntityRepository<Book> {
}
