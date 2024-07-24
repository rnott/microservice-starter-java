package org.rnott.example;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.rnott.example.api.EntityMetadata;
import org.rnott.example.api.EntityState;
import org.rnott.example.api.Example;
import org.rnott.example.api.PageOfExamples;
import org.rnott.example.persistence.ExampleEntity;
import org.rnott.example.persistence.ExampleMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class ConverterTest {
    @Test
    void serviceEntityCanBeConvertedToApiType() {
        ExampleMapper mapper = ExampleMapper.INSTANCE;
        ExampleEntity entity = ExampleEntity.builder()
                .id(UUID.randomUUID())
                .name("foo")
                .state(EntityState.DISABLED)
                .description("sample")
                .created(OffsetDateTime.now())
                .createdBy("me")
                .modified(OffsetDateTime.now())
                .modifiedBy("you")
                .version(99)
                .tag("foo", "bar")
                .tag("rank", "1")
                .build();
        Example rec = mapper.toApi(entity);
        assert rec != null;
        assert entity.getId().equals(rec.getId());
        assert entity.getName().equals(rec.getName());
        assert entity.getDescription().equals(rec.getDescription());
        assert entity.getState() == rec.getState();
        assert entity.getVersion() == rec.getVersion();
        EntityMetadata metadata = rec.getMetadata();
        assert entity.getCreated().equals(metadata.getCreated());
        assert entity.getCreatedBy().equals(metadata.getCreatedBy());
        assert entity.getModified().equals(metadata.getModified());
        assert entity.getModifiedBy().equals(metadata.getModifiedBy());
    }

    @Test
    void apiTypeCanBeConvertedToServiceType() {
        Example example = new Example();
        example.id(UUID.randomUUID());
        example.name("foo")
                .description("sample")
                .state(EntityState.DISABLED)
                .version(99L)
                .metadata(
                        new EntityMetadata()
                                .created(OffsetDateTime.now())
                                .createdBy("me")
                                .modified(OffsetDateTime.now())
                                .modifiedBy("you")
                );

        ExampleEntity entity = ExampleMapper.INSTANCE.toEntity(example);

        assert example.getId().equals(entity.getId());
        assert example.getName().equals(entity.getName());
        assert example.getDescription().equals(entity.getDescription());
        assert example.getState() == entity.getState();
        assert example.getVersion() != null;
        assert example.getVersion() == entity.getVersion();
        EntityMetadata metadata = example.getMetadata();
        assert entity.getCreated() == null;
        assert entity.getCreatedBy() == null;
        assert entity.getModified() == null;
        assert entity.getModifiedBy() == null;
    }

    @Test
    void entityMetadataCanBeMerged() {
        ExampleEntity current = ExampleEntity.builder()
                .created(Instant.now().atOffset(ZoneOffset.UTC))
                .createdBy("me")
                .modified(Instant.now().atOffset(ZoneOffset.UTC))
                .modifiedBy("you")
                .build();
        ExampleEntity entity = new ExampleEntity();
        ExampleMapper.INSTANCE.mergeMetadata(current, entity);
        assert current.getCreated().equals(entity.getCreated());
        assert current.getCreatedBy().equals(entity.getCreatedBy());
        assert current.getModified().equals(entity.getModified());
        assert current.getModifiedBy().equals(entity.getModifiedBy());
    }

    @Test
    void pageOfEntitiesCanBeConvertedToApiPage() {
        Page<ExampleEntity> source = new PageImpl<>(
                List.of(
                        ExampleEntity.builder()
                                .name("foo")
                                .version(99)
                                .created(Instant.now().atOffset(ZoneOffset.UTC))
                                .createdBy("me")
                                .modified(Instant.now().atOffset(ZoneOffset.UTC))
                                .modifiedBy("you")
                                .build(),
                        ExampleEntity.builder()
                                .name("foo")
                                .version(99)
                                .created(Instant.now().atOffset(ZoneOffset.UTC))
                                .createdBy("me")
                                .modified(Instant.now().atOffset(ZoneOffset.UTC))
                                .modifiedBy("you")
                                .build()
                ),
                Pageable.ofSize(2),
                100
        );
        PageOfExamples page = ExampleMapper.INSTANCE.toPage(source);
        assert page != null;
        assert page.getTotalCount() == 100;
        assert page.getResultCount() == 2;
        assert page.getCurrentPage() == 1;
        assert page.getPageSize() == 2;
        List<Example> data = page.getData();
        assert data != null;
        assert data.size() == 2;
        // TODO: assert element mapping
    }
}
