package org.rnott.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "authors")
public class Author extends AbstractEntity {

    @Column(nullable = false)
    private String firstName = "";
    @Column(nullable = false)
    private String lastName = "";
    @Column
    private LocalDate birthDate = null;

    public Author(String gn, String sn, LocalDate bd) {
        this(gn, sn, bd, EntityState.ACTIVE, new LinkedHashMap<>());
    }

    @Builder
    public Author(String gn, String sn, LocalDate bd, EntityState state, Map<String, String> tags) {
        super(null, state, null, null, null, null, null, tags);
        this.firstName = gn;
        this.lastName = sn;
        this.birthDate = bd;
    }
}
