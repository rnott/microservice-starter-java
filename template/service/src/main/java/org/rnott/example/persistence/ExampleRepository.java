package org.rnott.example.persistence;

import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRepository extends AbstractEntityRepository<ExampleEntity> {
}
