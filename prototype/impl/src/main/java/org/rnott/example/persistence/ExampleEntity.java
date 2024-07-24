package org.rnott.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.rnott.example.api.EntityState;

@Entity
@Table(name = "examples")
@NoArgsConstructor
@Getter
@Setter
public class ExampleEntity extends AbstractEntity {
    @Column(nullable = false)
    @NotNull
    private String name;

    @Column
    private String description;

    @Builder
    public ExampleEntity(
            @NotNull String name,
            String description,
            UUID id,
            EntityState state,
            OffsetDateTime created,
            String createdBy,
            OffsetDateTime modified,
            String modifiedBy,
            Integer version,
            @Singular Map<String, String> tags
    ) {
        super(id, state, created, createdBy, modified, modifiedBy, version, tags);
        this.name = name;
        this.description = description;
    }
}
