package org.rnott.example.persistence;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test the features of the {@link AbstractEntityRepository}. Only the
 * features that have been added or overridden are tested, assuming
 * upstream (Spring Data) features work as advertised.
 *
 * There are separate test suites to test the soft-delete and search features
 * in detail.
 * @see SoftDeleteTest
 * @see SearchTest
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class RepositoryTest {

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

    private final List<Author> savedAuthors = new LinkedList<>();
    private final List<Book> savedBooks = new LinkedList<>();
    private final List<Category> savedCategories = new LinkedList<>();


    @Autowired
    private TestEntityManager entityManager;

    @BeforeAll
    static void init() {
        postgres.start();
    }
    @AfterAll
    static void shutdown() {
        postgres.stop();
        postgres.close();
    }

    @Sql("truncate_tables.sql")
    @BeforeEach
    void setup() {
        List<Author> authors = List.of(
                new Author("Dan","Brown", LocalDate.of(1964, 6, 22)),
                new Author("J.K.", "Rowling", LocalDate.of(1965, 7, 31))
        );
        for (Author a : authors) {
            entityManager.getEntityManager().persist(a);
            savedAuthors.add(a);
        }
        assert savedAuthors.size() == 2;

        List<Category> categories = List.of(
                new Category("Fantasy"),
                new Category("Mystery"),
                new Category("Thriller")
        );
        for (Category c : categories) {
            entityManager.getEntityManager().persist(c);
            savedCategories.add(c);
        }
        assert savedCategories.size() == 3;

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
        for (Book b : books) {
            entityManager.getEntityManager().persist(b);
            savedBooks.add(b);
        }
        assert savedBooks.size() == 3;
    }

    @Test
    void canFindMultipleEntitiesInTheCollection() {
        List<Category> all = categoryRepository.findAll();
        assert all.size() == 3;
        assert all.stream().filter(b -> b.getId() != null)
                .count() == 3;
        all = categoryRepository.findAll(Sort.by(Order.asc("name")));
        assert all.size() == 3;
        all = categoryRepository.findAll(Pageable.ofSize(10)).getContent();
        assert all.size() == 3;

        List<UUID> ids = all.stream()
                .map(AbstractEntity::getId)
                .toList();
        all = categoryRepository.findAllById(ids);
        assert all.size() == 3;
        assert categoryRepository.count() == ids.size();
    }

    @Test
    void canFindEntitiesByIdentity() {
        List<Category> all = entityManager.getEntityManager()
                .createQuery("select e from Category e", Category.class)
                .getResultList();
        assert all.size() == 3;

        for (Category it : all) {
            Optional<Category> c = categoryRepository.findById(it.getId());
            assert c.isPresent();
            assert c.get().getId().equals(it.getId());

            boolean exists = categoryRepository.existsById(it.getId());
            assert exists;

            exists = categoryRepository.existsById(UUID.randomUUID());
            assert ! exists;
        }

        List<UUID> ids = all.stream()
                .map(AbstractEntity::getId)
                .toList();
        List<Category> found = categoryRepository.findAllById(ids);
        assert found.size() == ids.size();
        for (Category it : found) {
            assert ids.contains(it.getId());
        }
    }

    @Test
    void canManageEntities() {
        Category category = categoryRepository.save(
                Category.builder()
                        .name("foo")
                        .build()
        );
        assert category != null;
        assert category.getId() != null;
        assert "foo".equals(category.getName());
        assert ! category.isDeleted();
        assert category.getVersion() == 0;
        assert category.getCreated() != null;
        assert category.getCreatedBy() != null;
        assert category.getModified() != null;
        assert category.getModifiedBy() != null;
        assert category.getCreated().equals(category.getModified());
        assert category.getCreatedBy().equals(category.getModifiedBy());
    }
    @Test
    void canManageEntityTags() {
        UUID id = categoryRepository.findAll().stream()
                .map(AbstractEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no items"));
        Category category = categoryRepository.findByIdWithTags(id)
                .orElseThrow(() -> new IllegalStateException("id not found: " + id));
        assert category.getTags() != null;
        assert category.getTags().size() == 0;

        category.getTags().putAll(
                Map.of(
                        "foo", "bar",
                        "rank", "1",
                        // best practice not to use null in collections
                        // will substitute empty string instead
                        "important", ""

                )
        );
        categoryRepository.saveAndFlush(category);
        category = categoryRepository.findByIdWithTags(id)
                .orElseThrow(() -> new IllegalStateException("id not found"));
        assert category.getTags() != null;
        assert category.getTags().size() == 3;
        assert category.getTags().containsKey("foo");
        assert "bar".equals(category.getTags().get("foo"));
        assert category.getTags().containsKey("rank");
        assert "1".equals(category.getTags().get("rank"));
        assert category.getTags().containsKey("important");
        assert "".equals(category.getTags().get("important"));

        category.getTags().remove("important");
        categoryRepository.saveAndFlush(category);
        category = categoryRepository.findByIdWithTags(id)
                .orElseThrow(() -> new IllegalStateException("id not found"));
        assert category.getTags() != null;
        assert category.getTags().size() == 2;
        assert ! category.getTags().containsKey("important");

        category.getTags().clear();
        categoryRepository.saveAndFlush(category);
        category = categoryRepository.findByIdWithTags(id)
                .orElseThrow(() -> new IllegalStateException("id not found"));
        assert category.getTags() != null;
        assert category.getTags().size() == 0;
    }
}
