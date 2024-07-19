package org.rnott.example;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.rnott.example.api.Example;
import org.rnott.example.api.ExampleApi;
import org.rnott.example.api.PageOfExamples;
import org.rnott.example.api.PatchRequest;
import org.rnott.example.feature.Expires;
import org.rnott.example.persistence.ExampleEntity;
import org.rnott.example.persistence.ExampleMapper;
import org.rnott.example.persistence.ExampleRepository;
import org.rnott.example.persistence.SearchCriteria;
import org.rnott.example.persistence.SearchFactory;
import org.rnott.example.problems.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExampleApiImpl extends Application implements ExampleApi {

    @Context
    private UriInfo uriInfo;

    @Autowired
    private SearchFactory searchFactory;

    @Autowired
    private ExampleRepository repository;

    @Override
    public Map<String, Object> getProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearTags(UUID id) {
        ExampleEntity entity = repository.findByIdWithTags(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        entity.getTags().clear();
        repository.save(entity);
    }

    @Override
    public Example create(Example example) {
        ExampleEntity entity = ExampleMapper.INSTANCE.toEntity(example);
        return ExampleMapper.INSTANCE.toApi(
                repository.save(entity)
        );
    }

    @Override
    public void delete(UUID id) {
        // SD fails silently if entity does not exist (this is good)
        repository.deleteById(id);
    }

    @Override
    public Example fetch(UUID id) {
        ExampleEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        return ExampleMapper.INSTANCE.toApi(entity);
    }

    @Override
    public String fetchSingleTag(UUID id, String name) {
        ExampleEntity entity = repository.findByIdWithTags(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        if (entity.getTags().containsKey(name)) {
            return entity.getTags().get(name);
        }
        throw new NotFoundException( String.format("nane: %s", name));
    }

    @Override
    public Map<String, String> fetchTags(UUID id) {
        ExampleEntity entity = repository.findByIdWithTags(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        return entity.getTags();
    }

    @Override
    public Example patch(UUID id,  PatchRequest patchRequest) {
        ExampleEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        // TODO: implement patch
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeSingleTag(UUID id, String name) {
        ExampleEntity entity = repository.findByIdWithTags(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        var tags = entity.getTags();
                tags.remove(name);
        repository.save(entity);
    }

    @Override
    public void replaceSingleTag(UUID id, String name, String value) {
        ExampleEntity entity = repository.findByIdWithTags(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        entity.getTags().put(name, value);
        repository.save(entity);
    }

    @Override
    public void replaceTags(UUID id, Map<String, String> tags) {
        ExampleEntity entity = repository.findByIdWithTags(id)
                .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
        entity.getTags().clear();
        entity.getTags().putAll(tags);
       repository.save(entity);
    }

    @Override
    @Expires("pt5m0s")  // expire the cached result after 5 minutes
    public PageOfExamples search(
            List<UUID> id,
            Boolean deleted,
            Integer page,
            Integer limit,
            List<String> sort,
            String name
    ) {
        log.info("Search request: page={}, limit={}", page, limit);
        SearchCriteria.Builder<ExampleEntity> criteria = searchFactory.searchCriteriaBuilderFor(ExampleEntity.class);
        // common parameters
        if (deleted != null) {
            criteria.onlyDeletedEntities();
        }
        if (id != null && id.size() > 0) {
            criteria.optionsMatch("id", id.toArray(UUID[]::new));
        }
        if (sort != null) {
            criteria.orderAs(sort);
        }
        Pageable paging = PageRequest.of(
                page == null ? 0 : page - 1,  // api is 1-based while impl is 0-based
                limit == null ? 1000 : limit
        );

        /*
        other service defined parameters
         */
        if (name != null && name.length() > 0) {
            if (name.contains(SearchCriteria.WILDCARD)) {
                criteria.partialMatch("name", name);
            } else {
                criteria.exactMatch("name", name);
            }
        }
        Page<ExampleEntity> collection = repository.search(criteria.build(), paging);
        return ExampleMapper.INSTANCE.toPage(collection);
    }

    @Override
    public Example update(UUID id, Example example) {
        if (! id.equals(example.getId())) {
            throw new IllegalArgumentException("Identifier in the payload differs from the one in the path");
        }
        // support idempotency
        try {
            ExampleEntity current = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException(String.format("id: %s", id)));
            ExampleEntity entity = ExampleMapper.INSTANCE.toEntity(example);
            ExampleMapper.INSTANCE.mergeMetadata(current, entity);
            ExampleEntity result = repository.save(entity);
            return ExampleMapper.INSTANCE.toApi(result);
        } catch (NotFoundException e) {
            // create it
            return create(example);
        }
    }
}
