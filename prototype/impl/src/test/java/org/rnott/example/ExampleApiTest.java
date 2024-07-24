package org.rnott.example;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.rnott.example.api.EntityMetadata;
import org.rnott.example.api.EntityState;
import org.rnott.example.api.Example;
import org.rnott.example.api.PageOfExamples;
import org.rnott.example.persistence.ExampleEntity;
import org.rnott.example.persistence.ExampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ExampleApiTest {
    @Container
    private static final PostgreSQLContainer<?> pgContainer = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("integration-tests-db").withUsername("username").withPassword("password")
            .withInitScript("integration-data.sql");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", pgContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", pgContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", pgContainer::getPassword);
    }

    @LocalServerPort
    private int port;

    /**
     * Start all test containers.
     */
    @BeforeAll
    static void setup() {
        pgContainer.start();
    }

    /**
     * Stop all test containers.
     */
    @AfterAll
    static void shutdown() {
        pgContainer.stop();
        pgContainer.close();
    }

    /**
     * Configuration to be performed prior to each test.
     */
    @BeforeEach
    void configure() {
        RestAssured.baseURI = "http://localhost/api/service/v1";
        RestAssured.port = port;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Truncate the database tables after each test to eliminate
     * side effects.
     */
    @AfterEach
    void dbCleanup() {
        jdbcTemplate.execute("TRUNCATE TABLE examples CASCADE");
    }

    // used to populate the database by each test
    // repository features are separate unit tests
    @Autowired
    private ExampleRepository repository;

    @Test
    void serviceShouldAllowFetchingACollection() {
        repository.saveAllAndFlush(List.of(
                ExampleEntity.builder()
                        .name("foo")
                        .description("first example")
                        .version(99)
                        .build(),
                ExampleEntity.builder()
                        .name("bar")
                        .description("second example")
                        .version(99)
                        .build()
        ));
        var response = given().contentType(ContentType.JSON)
                .when()
                .get("/examples");
        assert response.statusCode() == 200;
        PageOfExamples result = response.as(PageOfExamples.class);
        assert result != null;
        assert result.getTotalCount() == 2;
        List<Example> data = result.getData();
        assert data != null;
        assert data.size() == 2;
        assert "foo".equals(data.get(0).getName());
        assert "first example".equals(data.get(0).getDescription());
        assert "bar".equals(data.get(1).getName());
        assert "second example".equals(data.get(1).getDescription());
        for (Example x : data) {
            assert x.getId() != null;
            assert x.getState() == EntityState.ACTIVE;
            assert x.getVersion() == 99;
            EntityMetadata metadata = x.getMetadata();
            assert metadata != null;
            assert metadata.getCreated() != null;
            assert metadata.getCreatedBy() != null;
            assert metadata.getModified() != null;
            assert metadata.getModifiedBy() != null;
        }
    }

    @Test
    void serviceShouldAllowCreationOfNewEntityInstances() {

        UUID id = UUID.randomUUID();
        Example source = new Example();
        source.id(id);
        source.name("foo")
                .description("An example");
        var response = given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .post("/examples");
        assert response.statusCode() == 200;
        Example x = response.as(Example.class);
        assert x != null;
        assert id.equals(x.getId());
        assert source.getName().equals(x.getName());
        assert source.getDescription().equals(x.getDescription());
        assert source.getState() == x.getState();
        assert x.getVersion() == 0;
        EntityMetadata metadata = x.getMetadata();
        assert metadata != null;
        assert metadata.getCreated() != null;
        assert metadata.getModified() != null;
        assert metadata.getModified() != null;
        assert metadata.getModifiedBy() != null;
        assert metadata.getCreated().equals(metadata.getModified());
        assert metadata.getCreatedBy().equals(metadata.getModifiedBy());
    }

    @Test
    void serviceShouldAllowFetchingAnInstanceByIdentity() {
        UUID id = UUID.randomUUID();
        repository.saveAllAndFlush(List.of(
                ExampleEntity.builder()
                        .id(id)
                        .name("foo")
                        .description("first example")
                        .version(99)
                        .build(),
                ExampleEntity.builder()
                        .name("bar")
                        .description("second example")
                        .version(99)
                        .build()
        ));

        var response = given().contentType(ContentType.JSON)
                .when()
                .get("/examples/{id}", id);
        assert response.statusCode() == 200;
        Example result = response.as(Example.class);
        assert result != null;
        assert id.equals(result.getId());
        assert "foo".equals(result.getName());
        assert "first example".equals(result.getDescription());
        assert result.getState() == EntityState.ACTIVE;
        assert result.getVersion() == 99;
        EntityMetadata metadata = result.getMetadata();
        assert metadata != null;
        assert metadata.getCreated() != null;
        assert metadata.getCreatedBy() != null;
        assert metadata.getModified() != null;
        assert metadata.getModifiedBy() != null;
    }

    @Test
    void serviceShouldAllowUpdatingAnInstanceByIdentity() {
        UUID id = UUID.randomUUID();
        ExampleEntity source = repository.saveAndFlush(
                ExampleEntity.builder()
                        .id(id)
                        .name("foo")
                        .description("first example")
                        .version(99)
                        .build()
        );

        var example = new Example();
        example.id(id);
        var response = given()
                .contentType(ContentType.JSON)
                .body(example
                        .name(source.getName())
                        .description("updated example")
                        .version(99L)
                )
                .when()
                .put("/examples/{id}", id);
        assert response.statusCode() == 200;
        Example result = response.as(Example.class);
        assert result != null;
        assert id.equals(result.getId());
        assert "foo".equals(result.getName());
        assert "updated example".equals(result.getDescription());
        assert result.getState() == source.getState();
        assert result.getVersion() == source.getVersion() + 1;
        EntityMetadata metadata = result.getMetadata();
        assert metadata != null;
        assert metadata.getCreated() != null;
        assert metadata.getCreated().equals(source.getCreated());
        assert metadata.getCreatedBy() != null;
        assert metadata.getCreatedBy().equals(source.getCreatedBy());
        assert metadata.getModified() != null;
        assert metadata.getModified().isAfter(source.getModified());
        assert metadata.getModified().isAfter(metadata.getCreated());
        assert metadata.getModifiedBy() != null;
    }

    @Test
    void serviceShouldAllowDeletingAnInstanceByIdentity() {
        UUID id = UUID.randomUUID();
        Example source = new Example();
        source.id(id);
        source.name("foo")
                .description("An example");
        var response = given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .put("/examples/{id}", id);
        assert response.statusCode() == 200;
        Example x = response.as(Example.class);
        assert id.equals(x.getId());

        given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .delete("/examples/{id}", id)
                .then()
                .statusCode(204);
        given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .get("/examples/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void serviceShouldAllowFetchingEntityMetadataTags() {
        UUID id = UUID.randomUUID();
        var foo = repository.saveAllAndFlush(List.of(
                ExampleEntity.builder()
                        .id(id)
                        .name("foo")
                        .description("first example")
                        .tags(Map.of(
                                "foo", "bar",
                                "rank", "1"
                        ))
                        .build()
        ));

        var response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/examples/{id}/tags", id)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();
        Map<String, String> tags = response.as(new TypeRef<Map<String, String>>() {});
        assert tags != null;
        assert tags.size() == 2;
        assert tags.containsKey("foo");
        assert "bar".equals(tags.get("foo"));
        assert tags.containsKey("rank");
        assert "1".equals(tags.get("rank"));
    }

    @Test
    void serviceShouldAllowMaintainingEntityMetadataTags() {
        UUID id = UUID.randomUUID();
        repository.saveAllAndFlush(List.of(
                ExampleEntity.builder()
                        .id(id)
                        .name("foo")
                        .description("first example")
                        .tags(Map.of(
                                "foo", "bar",
                                "rank", "1"
                        ))
                        .build()
        ));
        given()
                .when()
                .delete("/examples/{id}/tags/{name}", id, "foo")
                .then()
                .statusCode(204);
        given()
                .when()
                .delete("/examples/{id}/tags/{name}", id, "rank")
                .then()
                .statusCode(204);
        //entityManager.getEntityManager().clear();
        var response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/examples/{id}/tags", id)
                .then()
                .statusCode(200)
                .extract().response();
        Map<String, String> tags = response.as(new TypeRef<Map<String, String>>() {});
        assert tags != null;
        assert tags.size() == 0;

        // clear tags
        given()
                .when()
                .delete("/examples/{id}/tags", id)
                .then()
                .statusCode(204);
        response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/examples/{id}/tags", id)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();
        tags = response.as(new TypeRef<Map<String, String>>() {});
        assert tags != null;
        assert tags.size() == 0;
    }

    @Test
    void serviceDetectsVersionBasedConflicts() {
        ExampleEntity source = repository.saveAndFlush(
                ExampleEntity.builder()
                        .name("foo")
                        .description("first example")
                        .version(1)
                        .build()
        );

        var example = new Example();
        example.id(source.getId());
        var response = given()
                .contentType(ContentType.JSON)
                .body(example.name(source.getName())
                        .description("updated example")
                        .version(0L)
                )
                .when()
                .put("/examples/{id}", source.getId())
                .then()
                .statusCode(409);
    }

    @Test
    void serviceGeneratesMissingIdentifierOnCreate() {
        // generated
        Example source = new Example()
                .name("foo")
                .description("An example");
        var response = given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .post("/examples");
        assert response.statusCode() == 200;
        Example x = response.as(Example.class);
        assert x.getId() != null;

        // provided
        UUID id = UUID.randomUUID();
        source = new Example();
        source.id(id);
        source.name("foo")
                .description("An example");
        response = given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .post("/examples");
        assert response.statusCode() == 200;
        x = response.as(Example.class);
        assert id.equals(x.getId());
    }

    @Test
    void serviceMethodsAreIdempotent() {
        // use PUT to create
        UUID id = UUID.randomUUID();
        Example source = new Example();
        source.id(id);
        source.name("foo")
                .description("An example");
        var response = given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .put("/examples/{id}", id);
        assert response.statusCode() == 200;
        Example x = response.as(Example.class);
        assert id.equals(x.getId());

        // multiple deletes are successful
        given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .delete("/examples/{id}", id)
                .then()
                .statusCode(204);
        given()
                .contentType(ContentType.JSON).body(source)
                .when()
                .delete("/examples/{id}", id)
                .then()
                .statusCode(204);
    }
}
