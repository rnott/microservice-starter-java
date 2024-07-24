package org.rnott.example.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.rnott.example.api.EntityState;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "books")
public class Book extends AbstractEntity {

    @Column(nullable = false)
    private String title = "";

    @Column
    private LocalDate publishedDate;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "author_id", referencedColumnName = "id")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "false", referencedColumnName = "deleted"))
    })
    private Author author;

    @ManyToMany
    @JoinTable(
            name = "book_categories",
            joinColumns = {@JoinColumn(name = "book_id")},
            inverseJoinColumns = {@JoinColumn(name = "category_id")}
    )
    @Where(clause = "deleted = false")
    @Column
    private List<Category> categories = new LinkedList<>();

    public Book(String title, LocalDate publishedDate,
            Author author, @Singular List<Category> categories) {
        this(EntityState.ACTIVE, new LinkedHashMap<>(), title, publishedDate, author, categories);
    }

    @Builder
    public Book(EntityState state, Map<String, String> tags, String title, LocalDate publishedDate,
            Author author, @Singular List<Category> categories) {
        super(null, state, null, null, null, null, null, tags);
        this.title = title;
        this.publishedDate = publishedDate;
        this.author = author;
        this.categories = categories;
    }
}
