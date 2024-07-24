package org.rnott.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.rnott.example.api.EntityState;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category extends AbstractEntity {
    @Column
    private String name = "";

    public Category(String name) {
        this(EntityState.ACTIVE, new LinkedHashMap<>(), name);
    }

    @Builder
    public Category(EntityState state, Map<String, String> tags, String name) {
        super(null, state, null, null, null, null, null, tags);
        this.name = name;
    }
}
