package org.rnott.example.persistence;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractEntityRepository<T extends AbstractEntity> extends JpaRepository<T, UUID> {

    /**
     * Default page size for bounding queries.
     */
    int DEFAULT_PAGE_SIZE = 1000;

    /*
     only fetch non-deleted entities
     */

    @Override
    @NotNull
    @Query("select e from #{#entityName} e where e.deleted = false")
    List<T> findAll();

    @Override
    @NotNull
    @Query("select e from #{#entityName} e where e.deleted = false")
    List<T> findAll(@NotNull Sort sort);

    @Override
    @Query("select e from #{#entityName} e where e.deleted = false")
    @NotNull
    Page<T> findAll(@NotNull Pageable paging);

    @Query("select e from #{#entityName} e where e.deleted = false and e.id = ?1")
    @Override
    @NotNull
    Optional<T> findById(@NotNull UUID id);

    @Override
    @Query("select case when (count(id) > 0)  then true else false end from #{#entityName} e where e.id = ?1")
    boolean existsById(@NotNull UUID id);

    @Override
    @Query("select count(e) from #{#entityName} e where e.deleted = false")
    long count();

    /*
     soft delete (e.g. tomb-stoned entities)
     TODO: batch
        deleteAllInBatch();
        deleteAllInBatch(entities);
        deleteAllByIdInBatch(ids);
        undeleteAllInBatch();
        undeleteAllInBatch(entities);
        undeleteAllByIdInBatch(ids);
     */

    @Override
    @Modifying
    @Query("update #{#entityName} e set e.deleted = true where e.deleted = false")
    void deleteAll();

    @Override
    @Modifying
    @Query("update #{#entityName} e set e.deleted = true where e.id in ?1 and e.deleted = false")
    void deleteAllById(@NotNull Iterable<? extends UUID> ids);

    @Override
    @Modifying
    @Query("update #{#entityName} e set e.deleted = true where e.id = ?1 and e.deleted = false")
    void deleteById(@NotNull UUID id);

    /**
     * Query all entities that have been soft-deleted.
     *
     * @return the collection of soft-deleted entities.
     */
    @Query("select e from #{#entityName} e where e.deleted = true")
    List<T> findAllDeleted();

    /**
     * Determine the number of soft-deleted entities.
     *
     * @return the number of entities that are soft-deleted
     */
    @Query("select count(e) from #{#entityName} e where e.deleted = true")
    long countAllDeleted();

    /**
     * Resurrect all soft-deleted entities.
     */
    @Modifying
    @Query("update #{#entityName} e set e.deleted = false where e.deleted = true")
    void undeleteAll();

    /**
     * Resurrects all the specified soft-deleted entities.
     *
     * @param ids the identifiers of the entities to resurrect
     */
    @Modifying
    @Query("update #{#entityName} e set e.deleted = false where e.id in ?1 and e.deleted = true")
    void undeleteAllById(Iterable<UUID> ids);

    /**
     * Resurrects a soft-deleted entity with the specified identifier.
     *
     * @param id the identifier of the entity to resurrect
     */
    @Modifying
    @Query("update #{#entityName} e set e.deleted = false where e.id = ?1 and e.deleted = true")
    void undeleteById(@NotNull UUID id);

    /*
    custom queries
     */

    @Query("select e from #{#entityName} e left join fetch e.tags where e.deleted = false and e.id = ?1")
    Optional<T> findByIdWithTags(@NotNull UUID id);

   /**
     * Search for entities based on the specified criteria. Results are
     * bounded using the default page size. Note that deleted items are
     * implemented by the criteria and there is no clause used here.
     *
     * @param criteria the criteria to apply to the query
     * @return a page of entities matching the specified criteria
     * @see #DEFAULT_PAGE_SIZE
     */
    @NotNull
    default Page<T> search(@NotNull SearchCriteria<T> criteria) {
        return search(criteria, Pageable.ofSize(DEFAULT_PAGE_SIZE));
    }

    /**
     * Search for entities based on the specified criteria. Note that
     * deleted items are implemented by the criteria and there is no
     * clause used here.
     *
     * @param criteria the criteria to apply to the query
     * @param paging indicates the page of data to be returned
     * @return a page of entities matching the specified criteria
     */
    @NotNull
    default Page<T> search(@NotNull SearchCriteria<T> criteria, @NotNull Pageable paging) {
        List<T> results = criteria.getResultsQuery()
                .setFirstResult((int) paging.getOffset())
                .setMaxResults(paging.getPageSize())
                .getResultList();
        long count = criteria.getCountQuery().getSingleResult();
        return new PageImpl<>(results, paging, count);
    }
}
