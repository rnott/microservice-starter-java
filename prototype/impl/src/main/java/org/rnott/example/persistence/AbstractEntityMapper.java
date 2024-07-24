package org.rnott.example.persistence;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.rnott.example.api.PageOfExamples;
import org.springframework.data.domain.Page;

/**
 * Base converter that manges the mapping between API types and entities.
 *
 * @param <A> the API type
 * @param <PA> the corresponding API collection type
 * @param <E> the entity type
 */
public interface AbstractEntityMapper<A, PA extends PageOfExamples, E extends AbstractEntity> {

    /**
     * Converts an entity to an API type. This configuration handles all the fields
     * from the common base class for entities. Use this as the starting point
     * for new service entities.
     *
     * @param entity the entity to be converted
     * @return the entity converted to API format
     * @see AbstractEntity
     */
    @Mapping(source = "created", target = "metadata.created")
    @Mapping(source = "createdBy", target = "metadata.createdBy")
    @Mapping(source = "modified", target = "metadata.modified")
    @Mapping(source = "modifiedBy", target = "metadata.modifiedBy")
    A toApi(E entity);

    /**
     * Converts an API type to a service entity. This configuration handles all the fields
     * from the common base class for entities. Use this as the starting point
     * for new service entities.
     * <p>
     * Metadata in the source is ignored as it is read-only.
     *
     * @param api the API type to be converted
     * @return the API type converted to entity format
     * @see AbstractEntity
     */
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modified", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    E toEntity(A api);

    /**
     * Merges the current entity metadata with the target entity.
     *
     * @param current the current entity state
     * @param target the target entity
     */
    default void mergeMetadata(E current, E target) {
        target.setCreated(current.getCreated());
        target.setCreatedBy(current.getCreatedBy());
        target.setModified(current.getModified());
        target.setModifiedBy(current.getModifiedBy());
    }

    /**
     * Converts a Spring Data page to API format.
     *
     * @param result the Spring Data page to be converted
     * @return the page converted to an API type
     */
    @Mapping(source = "totalElements", target = "totalCount")
    @Mapping(source = "content", target = "data")
    @Mapping(source = "numberOfElements", target = "resultCount")
    @Mapping(source = "totalPages", target = "lastPage")
    @Mapping(source = "pageable.pageSize", target = "pageSize")
    @Mapping(source = "pageable.pageNumber", target = "currentPage")
    @Mapping(target = "first", ignore = true)
    @Mapping(target = "last", ignore = true)
    @Mapping(target = "next", ignore = true)
    @Mapping(target = "previous", ignore = true)
    PA toPage(Page<E> result);

    @AfterMapping
    default void calculatePage(Page<ExampleEntity> source, @MappingTarget PA page) {
        // SpringData pages start at zero, should start at 1
        page.currentPage(source.getNumber() + 1)
                .lastPage(source.getTotalPages() + 1);
    }
}
