package org.rnott.example.persistence;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test search features.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SearchTest {

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
    private SearchFactory searchFactory;

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
        postgres.close();
    }

    @Sql("truncate_tables.sql")
    @BeforeEach
    void setup() {
        List<Author> authors = List.of(
                new Author("Dan", "Brown", LocalDate.of(1964, 6, 22)),
                new Author("J.K.", "Rowling", LocalDate.of(1965, 7, 31))
        );
        savedAuthors.addAll(authorRepository.saveAll(authors));
        assert savedAuthors.size() == 2;

        List<Category> categories = List.of(
                new Category("Fantasy"),
                new Category("Mystery"),
                new Category("Thriller")
        );
        savedCategories.addAll(categoryRepository.saveAll(categories));
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
        savedBooks.addAll(bookRepository.saveAll(books));
        assert savedBooks.size() == 3;
    }

    @Test
    void searchCanFilterUsingExactMatching() {
        // expect matches
        SearchCriteria<Author> criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .exactMatch("lastName", "Brown")
                .build();
        Page<Author> results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;
        List<Author> list = results.toList();
        assert list.size() == 1;

        // expect no matches
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .exactMatch("lastName", "Unknown")
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 0;
        list = results.toList();
        assert list.size() == 0;
    }

    @Test
    void searchCanFilterUsingExactMatchingOfMultipleProperties() {
        // expect matches
        SearchCriteria<Author> criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .exactMatch("firstName", "Dan")
                .exactMatch("lastName", "Brown")
                .build();
        Page<Author> results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;
        List<Author> list = results.toList();
        assert list.size() == 1;

        // expect no matches
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .exactMatch("firstName", "Sam")
                .exactMatch("lastName", "Brown")
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 0;
        list = results.toList();
        assert list.size() == 0;
    }

    @Test
    void searchCanFilterOnPartialMatching() {
        // expect matches
        SearchCriteria<Author> criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .partialMatch("lastName", SearchCriteria.WILDCARD + "row" + SearchCriteria.WILDCARD)
                .build();
        Page<Author> results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;
        assert "Brown".equals(results.toList().get(0).getLastName());

        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .partialMatch("lastName", SearchCriteria.WILDCARD + "own")
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;

        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .partialMatch("lastName", "Bro" + SearchCriteria.WILDCARD)
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;

        // expect no matches
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .partialMatch("lastName", SearchCriteria.WILDCARD + "r" + SearchCriteria.WILDCARD + "wn")
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;

        // expect no matches
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .partialMatch("lastName", SearchCriteria.WILDCARD + "raw" + SearchCriteria.WILDCARD)
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 0;
    }

    @Test
    void searchCanFilterUsingValueOptions() {
        // expect matches
        SearchCriteria<Author> criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .optionsMatch("lastName", "Brown", "Unknown", "Asimov")
                .build();
        Page<Author> results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;
        List<Author> list = results.toList();
        assert list.size() == 1;

        // expect no matches
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .optionsMatch("lastName", "Unknown", "Asimov")
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 0;
        list = results.toList();
        assert list.size() == 0;
    }

    @Test
    void searchCanFilterUsingAValueRange() {
        SearchCriteria<Book> criteria = searchFactory.searchCriteriaBuilderFor(Book.class)
                .rangeMatch(
                        "publishedDate",
                        LocalDate.of(2003, 1, 1),
                        LocalDate.of(2006, 1, 1)
                )
                .build();
        Page<Book> results = bookRepository.search(criteria);
        assert results.getTotalElements() == 2;
        List<Book> books = results.toList();
        assert books.size() == 2;
        assert "The Da Vinci Code".equals(books.get(0).getTitle());
        assert "Harry Potter and the Half-Blood Prince".equals(books.get(1).getTitle());
    }

    @Test
    void resultsCanBePaged() {
        // need more stuff for paging
        for (int i = 1; i <= 100; i++) {
            categoryRepository.save(new Category(
                    "Category" + i
            ));
        }

        SearchCriteria<Category> criteria = searchFactory.searchCriteriaBuilderFor(Category.class)
                // exclude what we deleted
                .exactMatch("deleted", false)
                .build();
        Page<Category> results;
        Pageable paging = Pageable.ofSize(25);
        int pages = 0;
        do {
            results = categoryRepository.search(criteria, paging);
            assert results.getPageable().getPageNumber() == pages;
            pages++;
            assert results.getTotalElements() == 103;
            assert results.stream().toList().size() == (results.isLast() ? 3 : 25);
            paging = results.nextPageable();
        } while (!results.isLast());
        assert pages == 5;
    }

    @Test
    void resultsCanBeSorted() {
        // descending
        List<String> sort = List.of("-lastName");
        SearchCriteria<Author> criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .orderAs(sort)
                .build();
        Page<Author> page = authorRepository.search(criteria);
        List<Author> results = page.getContent();
        assert results.size() == 2;
        assert "Rowling".equals(results.get(0).getLastName());
        assert "Brown".equals(results.get(1).getLastName());

        // ascending
        sort = List.of("+lastName");
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .orderAs(sort)
                .build();
        page = authorRepository.search(criteria);
        results = page.getContent();
        assert results.size() == 2;
        assert "Brown".equals(results.get(0).getLastName());
        assert "Rowling".equals(results.get(1).getLastName());

        // default (ascending)
        sort = List.of("lastName");
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .orderAs(sort)
                .build();
        page = authorRepository.search(criteria);
        results = page.getContent();
        assert results.size() == 2;
        assert "Brown".equals(results.get(0).getLastName());
        assert "Rowling".equals(results.get(1).getLastName());
    }

    @Test
    void searchCanBeScopedToSoftDeletedEntities() {
        // delete all the authors
        for (Author a : savedAuthors) {
            authorRepository.deleteById(a.getId());
        }
        // confirm none are found when using default scope
        SearchCriteria<Author> criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .exactMatch("lastName", "Brown")
                .build();
        Page<Author> results = authorRepository.search(criteria);
        assert results.getTotalElements() == 0;
        List<Author> list = results.toList();
        assert list.size() == 0;

        // confirm deleted scope
        criteria = searchFactory.searchCriteriaBuilderFor(Author.class)
                .onlyDeletedEntities()
                .exactMatch("lastName", "Brown")
                .build();
        results = authorRepository.search(criteria);
        assert results.getTotalElements() == 1;
        list = results.toList();
        assert list.size() == 1;
    }

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void canSearchByTags() {
        Category c = savedCategories.stream()
                .filter(it -> "Fantasy".equals(it.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("category not found"));
        c.getTags().put("important", "");
        c.getTags().put("rating", "3.9");
        c.getTags().put("classification", "classic");
        categoryRepository.save(c);

        Page<Category> page = categoryRepository.search(
                searchFactory.searchCriteriaBuilderFor(Category.class)
                        .tagPresent("important")
                        .build()
        );
        assert page.getTotalElements() == 1;
        List<Category> results = page.toList();
        assert results.size() == 1;
        assert "Fantasy".equals(results.get(0).getName());

        page = categoryRepository.search(
                searchFactory.searchCriteriaBuilderFor(Category.class)
                        .tagExactMatch("classification", "classic")
                        .build()
        );
        assert page.getTotalElements() == 1;
        results = page.toList();
        assert results.size() == 1;
        assert "Fantasy".equals(results.get(0).getName());

        page = categoryRepository.search(
                searchFactory.searchCriteriaBuilderFor(Category.class)
                        .tagRangeMatch("rating", "3.0", "4.0")
                        .build()
        );
        assert page.getTotalElements() == 1;
        results = page.toList();
        assert results.size() == 1;
        assert "Fantasy".equals(results.get(0).getName());

        page = categoryRepository.search(
                searchFactory.searchCriteriaBuilderFor(Category.class)
                        .tagOptionsMatch("classification", "new", "old", "classic")
                        .build()
        );
        assert page.getTotalElements() == 1;
        results = page.toList();
        assert results.size() == 1;
        assert "Fantasy".equals(results.get(0).getName());

        page = categoryRepository.search(
                searchFactory.searchCriteriaBuilderFor(Category.class)
                        .tagPartialMatch("classification", "%ass%")
                        .build()
        );
        assert page.getTotalElements() == 1;
        results = page.toList();
        assert results.size() == 1;
        assert "Fantasy".equals(results.get(0).getName());
    }
}
