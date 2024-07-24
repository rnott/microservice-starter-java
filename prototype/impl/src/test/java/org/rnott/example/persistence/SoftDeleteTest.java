package org.rnott.example.persistence;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Ensure soft deletion feature works as intended:
 * <ul>
 *     <li>inserted entities can be soft-deleted</li>
 *     <li>soft-deleted entites will not be retrieved by fetching, either
 *     by identity or collection</li>
 *     <li>soft-deleted entities will not be retrieved as part of a
 *     many-to-one relationship</li>
 *     <li>soft-deleted entities will not be retrieved as part of a
 *     many-to-many relationship</li>
 *     <li>soft-deleted can be fetched by explicit query</li>
 * </ul>
 * Inspired by <a href="https://medium.com/@nick-kling/soft-delete-with-spring-data-jpa-b6cf3bf1f89b">this article</a>.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SoftDeleteTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    ).withInitScript("test-data.sql");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("spring.datasource.url", postgres::getJdbcUrl);
        propertyRegistry.add("spring.datasource.password", postgres::getPassword);
        propertyRegistry.add("spring.datasource.username", postgres::getUsername);
    }

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final List<Author> savedAuthors = new LinkedList<>();
    private final List<Book> savedBooks = new LinkedList<>();
    private final List<Category> savedCategories = new LinkedList<>();

    @BeforeAll
    static void init() {
        postgres.start();
    }

    @AfterAll
    static void shutdown() {
        postgres.stop();
    }

    @Sql("truncate_tables.sql")
    @BeforeEach
    void setup() {
        List<Author> authors = List.of(
                new Author("Dan", "Brown", LocalDate.of(1964, 6, 22)),
                new Author("J.K.", "Rowling", LocalDate.of(1965, 7, 31))
        );
        authorRepository.saveAll(authors).forEach(savedAuthors::add);

        List<Category> categories = List.of(
                new Category("Fantasy"),
                new Category("Mystery"),
                new Category("Thriller")
        );
        categoryRepository.saveAll(categories).forEach(savedCategories::add);

        List<Book> books = List.of(
                new Book("The Da Vinci Code",
                        LocalDate.of(2003, 3, 18),
                        savedAuthors.stream()
                                .filter(it -> it.getLastName().equals("Brown"))
                                .findFirst().orElseThrow(() -> new IllegalStateException("author not found")),
                        categories = savedCategories.stream()
                                .filter(it -> it.getName().equals("Mystery")
                                        || it.getName().equals("Thriller"))
                                .toList()
                ),
                new Book(
                        "Harry Potter and the Deathly Hallows",
                        LocalDate.of(2007, 7, 21),
                        savedAuthors.stream()
                                .filter(it -> it.getLastName().equals("Rowling"))
                                .findFirst().orElseThrow(() -> new IllegalStateException("author not found")),
                        savedCategories.stream()
                                .filter(it -> it.getName().equals("Fantasy"))
                                .toList()
                ),
                new Book(
                        "Harry Potter and the Half-Blood Prince",
                        LocalDate.of(2005, 7, 16),
                        savedAuthors.stream()
                                .filter(it -> it.getLastName().equals("Rowling"))
                                .findFirst().orElseThrow(() -> new IllegalStateException("author not found")),
                        savedCategories.stream()
                                .filter(it -> it.getName().equals("Fantasy"))
                                .toList()
                )
        );
        bookRepository.saveAll(books).forEach(savedBooks::add);
    }

    @Test
    void insertedEntitiesCanBeSoftDeleted() {
        List<Category> retrievedBooks2 = categoryRepository.findAll();
        savedBooks.stream()
                .filter(it -> it.getTitle().equals("Harry Potter and the Half-Blood Prince"))
                .map(AbstractEntity::getId)
                .forEach(id -> bookRepository.deleteById(id));
        savedCategories.stream()
                .filter(it -> it.getName().equals("Mystery"))
                .map(AbstractEntity::getId)
                .forEach(id -> categoryRepository.deleteById(id));
        savedAuthors.stream()
                .filter(it -> it.getLastName().equals("Brown"))
                .map(AbstractEntity::getId)
                .forEach(id -> authorRepository.deleteById(id));

        List<Book> retrievedBooks = bookRepository.findAll();
        assert retrievedBooks.size() == 2;
        assert retrievedBooks.stream()
                .anyMatch(it -> it.getTitle().equals("The Da Vinci Code"));
        assert retrievedBooks.stream()
                .anyMatch(it -> it.getTitle().equals("Harry Potter and the Deathly Hallows"));
        assert retrievedBooks.stream()
                .noneMatch(it -> it.getTitle().equals("Harry Potter and the Half-Blood Prince"));

        List<Author> retrievedAuthors = authorRepository.findAll();
        assert retrievedAuthors.size() == 1;
        assert retrievedAuthors.stream()
                .anyMatch(it -> it.getLastName().equals("Rowling"));
        assert retrievedAuthors.stream()
                .noneMatch(it -> it.getLastName().equals("Brown"));

        List<Category> retrievedCategories = categoryRepository.findAll();
        assert retrievedCategories.size() == 2;
        assert retrievedCategories.stream()
                .anyMatch(it -> it.getName().equals("Fantasy"));
        assert retrievedCategories.stream()
                .anyMatch(it -> it.getName().equals("Thriller"));
        assert retrievedCategories.stream()
                .noneMatch(it -> it.getName().equals("Mystery"));
    }

    @Test
    void makeSureSoftDeletedObjectsAreMotReturnedAsPartOfAManyToOneAssociation() {
        savedAuthors.stream()
                .filter(it -> it.getLastName().equals("Brown"))
                .map(AbstractEntity::getId)
                .forEach(id -> authorRepository.deleteById(id));
        // Make sure that retrieved books are equal to state of DB
        entityManager.flush();
        entityManager.clear();

        List<Book> retrievedBooks = bookRepository.findAll();
        // present
        assert retrievedBooks.stream()
                .filter(it -> it.getTitle().equals("Harry Potter and the Deathly Hallows"))
                .map(Book::getAuthor)
                .anyMatch(Objects::nonNull);
        assert retrievedBooks.stream()
                .filter(it -> it.getTitle().equals("Harry Potter and the Half-Blood Prince"))
                .map(Book::getAuthor)
                .anyMatch(Objects::nonNull);
        // omitted
        assert retrievedBooks.stream()
                .filter(it -> it.getTitle().equals("The Da Vinci Code"))
                .map(Book::getAuthor)
                .noneMatch(Objects::nonNull);
    }

    @Test
    void makeSureSoftDeletedObjectsAreMotReturnedAsPartOfAManyToManyAssociation() {
        savedCategories.stream()
                .filter(it -> it.getName().equals("Mystery"))
                .map(Category::getId)
                .forEach(id -> categoryRepository.deleteById(id));

        // Make sure that retrieved books are equal to state of DB
        entityManager.flush();
        entityManager.clear();

        List<Book> retrievedBooks = bookRepository.findAll();
        List<Category> daVinciCodeCategories = retrievedBooks.stream()
                .filter(it -> it.getTitle().equals("The Da Vinci Code"))
                .flatMap(it -> it.getCategories().stream())
                .toList();
        List<Category> deathlyHallowsCategories = retrievedBooks.stream()
                .filter(it -> it.getTitle().equals("Harry Potter and the Deathly Hallows"))
                .flatMap(it -> it.getCategories().stream())
                .toList();
        List<Category> halfBloodPrinceCategories = retrievedBooks.stream()
                .filter(it -> it.getTitle().equals("Harry Potter and the Half-Blood Prince"))
                .flatMap(it -> it.getCategories().stream())
                .toList();
        assert daVinciCodeCategories.size() == 1;
        assert deathlyHallowsCategories.size() == 1;
        assert halfBloodPrinceCategories.size() == 1;
        assert daVinciCodeCategories.stream()
                .anyMatch(it -> it.getName().equals("Thriller"));
        assert daVinciCodeCategories.stream()
                .noneMatch(it -> it.getName().equals("Mystery"));
        assert deathlyHallowsCategories.stream()
                .anyMatch(it -> it.getName().equals("Fantasy"));
        assert halfBloodPrinceCategories.stream()
                .anyMatch(it -> it.getName().equals("Fantasy"));
    }

    @Test
    void deletedEntitiesWillCorrectlyBeRetrieved() {
        savedBooks.stream()
                .filter(it -> it.getTitle().equals("Harry Potter and the Half-Blood Prince"))
                .map(Book::getId)
                .forEach(id -> bookRepository.deleteById(id));
        savedCategories.stream()
                .filter(it -> it.getName().equals("Mystery"))
                .map(Category::getId)
                .forEach(id -> categoryRepository.deleteById(id));
        savedAuthors.stream()
                .filter(it -> it.getLastName().equals("Brown"))
                .map(Author::getId)
                .forEach(id -> authorRepository.deleteById(id));

        entityManager.flush();
        entityManager.clear();

        List<Book> retrievedBooks = bookRepository.findAllDeleted();
        assert retrievedBooks.size() == 1;
        assert retrievedBooks.stream()
                .anyMatch(it -> it.getTitle().equals("Harry Potter and the Half-Blood Prince"));

        List<Category> retrievedCategories = categoryRepository.findAllDeleted();
        assert retrievedCategories.size() == 1;
        assert retrievedCategories.stream()
                .anyMatch(it -> it.getName().equals("Mystery"));

        List<Author> retrievedAuthors = authorRepository.findAllDeleted();
        assert retrievedAuthors.size() == 1;
        assert retrievedAuthors.stream()
                .anyMatch(it -> it.getLastName().equals("Brown"));

        long count = authorRepository.countAllDeleted();
        assert count == retrievedAuthors.size();
    }

    @Test
    void hardDeletesAreNotSupported() {
        Category entity = savedCategories.get(0);
        try {
            categoryRepository.delete(entity);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert true;
        }

        try {
            entityManager.getEntityManager().remove(entity);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert true;
        }

        try {
            categoryRepository.deleteAll(savedCategories);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert true;
        }
    }

    @Test
    void singleEntitiesCanBeDeleted() {
        UUID id = savedCategories.get(0).getId();
        categoryRepository.deleteById(id);
        categoryRepository.flush();
        entityManager.getEntityManager().clear();
        Category deleted = entityManager.getEntityManager()
                .createQuery("select e from Category e where e.id = ?1", Category.class)
                .setParameter(1, id)
                .getSingleResult();
        assert deleted != null;
        assert deleted.isDeleted();
    }

    @Test
    void multipleEntitiesCanBeDeleted() {
        categoryRepository.deleteAll();
        categoryRepository.flush();
        entityManager.getEntityManager().clear();
        List<Category> all = entityManager.getEntityManager()
                .createQuery("select e from Category e", Category.class)
                .getResultList();
        for (Category it : all) {
            assert it.isDeleted();
        }

        List<UUID> ids = savedAuthors.stream()
                .map(AbstractEntity::getId)
                .toList();
        authorRepository.deleteAllById(ids);
        authorRepository.flush();
        entityManager.getEntityManager().clear();
        for (UUID id : ids) {
            Author a = entityManager.getEntityManager()
                    .createQuery("select e from Author e where e.id = ?1", Author.class)
                    .setParameter(1, id)
                    .getSingleResult();
            assert a.isDeleted();
        }
        /*
        TODO: batch
        Iterable<UUID> ids = new HashSet<>();
        Iterable<Category> entities = new HashSet<>();
        categoryRepository.deleteAllById(ids);
        categoryRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch(entities);
        categoryRepository.deleteAllByIdInBatch(ids);

         */
    }

    @Test
    void canResurrectedASingleDeletedEntity() throws Exception {
        Category c = entityManager.getEntityManager()
                .createQuery("select e from Category e where e.id = ?1", Category.class)
                .setParameter(1, savedCategories.get(0).getId())
                .getSingleResult();
        assert categoryRepository.findById(c.getId()).isPresent();
        categoryRepository.deleteById(c.getId());
        assert categoryRepository.findById(c.getId()).isEmpty();
        categoryRepository.undeleteById(c.getId());
        assert categoryRepository.findById(c.getId()).isPresent();
    }

    @Test
    void canResurrectedMultipleDeletedEntities() {
        List<Category> all = entityManager.getEntityManager()
                .createQuery("select e from Category e", Category.class)
                .getResultList();
        for (Category c : all) {
            categoryRepository.deleteById(c.getId());
            assert categoryRepository.findById(c.getId()).isEmpty();
        }
        categoryRepository.undeleteAll();
        categoryRepository.flush();
        entityManager.getEntityManager().clear();
        for (Category c : all) {
            assert categoryRepository.findById(c.getId()).isPresent();
        }

        all = entityManager.getEntityManager()
                .createQuery("select e from Category e", Category.class)
                .getResultList();
        for (Category c : all) {
            categoryRepository.deleteById(c.getId());
            assert categoryRepository.findById(c.getId()).isEmpty();
        }
        categoryRepository.flush();
        entityManager.getEntityManager().clear();
        categoryRepository.undeleteAllById(
                all.stream()
                        .map(AbstractEntity::getId)
                        .toList()
        );
        for (Category c : all) {
            assert categoryRepository.findById(c.getId()).isPresent();
        }
        /*
        TODO: batch
        Iterable<UUID> ids = new HashSet<>();
        Iterable<Category> entities = new HashSet<>();
        categoryRepository.undeleteAllInBatch();
        categoryRepository.undeleteAllInBatch(entities);
        categoryRepository.undeleteAllByIdInBatch(ids);

         */
    }
}
