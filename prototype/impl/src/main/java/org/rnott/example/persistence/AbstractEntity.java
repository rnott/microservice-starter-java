package org.rnott.example.persistence;

import static org.rnott.example.api.EntityState.ACTIVE;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.rnott.example.api.EntityState;
import org.wildfly.common.annotation.NotNull;

/*
When using Lombok:
- JPA requires default constructor
- avoid the use @EqualsAndHashCode as it tends to lead toward broken hash codes
  and will likely cause lazy loaded attributes to be loaded
- avoid the use of @ToString as it will likely cause lazy loaded attributes to be loaded
 */

/**
 * Entity base type. All entities should extend this type.
 */
@NoArgsConstructor
@Getter
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    protected AbstractEntity(
            UUID id,
            EntityState state,
            OffsetDateTime created,
            String createdBy,
            OffsetDateTime modified,
            String modifiedBy,
            Integer version,
            @Singular Map<String, String> tags
    ) {
        this.id = id;
        if (state != null) {
            this.state = state;
        }
        this.created = created;
        this.createdBy = createdBy;
        this.modified = modified;
        this.modifiedBy = modifiedBy;
        this.version = version == null ? 0 : version;
        this.tags = tags;
    }

    /**
     * Primary key (e.g. unique identifier).
     */
    @Id
    @NotNull
    @Column(nullable = false)
    private UUID id;

    /**
     * An optimistic lock used by JPA to detect concurrent modifications.
     */
    @Version
    private long version;

    // NOTE: could use ORDINAL to save space at the expense of readability
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityState state = ACTIVE;

    @Column
    private boolean deleted;

    @ElementCollection
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> tags = new LinkedHashMap<>();

    @Column
    @Setter
    private OffsetDateTime created;

    @Column
    @Setter
    private String createdBy;

    @Column
    @Setter
    private OffsetDateTime modified;

    @Column
    @Setter
    private String modifiedBy;


    /*
     *  The id field MUST be lazy generated when needed if it does not yet have a value:
         - hashCode()
         - equals()
         - pre-persistence
     */

    @PrePersist
    void insert() {
        // create an id if not assigned
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.created = Instant.now().atOffset(ZoneOffset.UTC);
        // TODO: integrate user
        this.createdBy = "unknown";
        this.modified = this.created;
        this.modifiedBy = this.createdBy;
    }

    @PreUpdate
    void update() {
        this.modified = Instant.now().atOffset(ZoneOffset.UTC);
        // TODO: integrate user
        this.modifiedBy = "unknown";
    }

    @PreRemove
    void remove() {
        /*
        Hard deletion is disallowed in favor of supporting soft-delete. You should be
        using a Repository that supports this.
        @see AbstractEntityRepository
         */
        throw new UnsupportedOperationException("Hard deletes are not supported");
    }

    /**
     * Declared final to prevent accidental loading of lazy attributes.
     *
     * @see Object#toString()
     */
    @Override
    public final String toString() {
        return super.toString();
    }

    /**
     * Declared final to prevent accidental loading of lazy attributes
     * and to ensure an identity based implementation, as one should expect
     * for persisted entities.
     *
     * @return a value based on the primary key
     * @see Object#hashCode()
     */
    @Override
    public final int hashCode() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        return this.id.hashCode();
    }

    /**
     * Declared final to prevent accidental loading of lazy attributes
     * and to ensure an identity based implementation, as one should expect
     * for persisted entities.
     *
     * @param o the instance to compare
     * @return <code>true</code> if the primary key of this object is
     * equal to the primary key of the other object, <code>false</code>
     * otherwise.
     * @see Object#equals(Object)
     */
    @Override
    public final boolean equals(Object o) {
        if (o != null && getClass().equals(o.getClass())) {
            if (this.id == null) {
                this.id = UUID.randomUUID();
            }
            return this.id.equals(((AbstractEntity) o).id);
        }
        return false;
    }
}
