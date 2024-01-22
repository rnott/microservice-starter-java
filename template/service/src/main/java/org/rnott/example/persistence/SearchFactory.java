package org.rnott.example.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.rnott.example.persistence.SearchCriteria.Builder;
import org.springframework.stereotype.Service;

@Service
public class SearchFactory {
    @PersistenceContext
    private EntityManager em;

    @Transactional(TxType.SUPPORTS)
    public  <T extends AbstractEntity> Builder<T> searchCriteriaBuilderFor(Class<T> clazz) {
        return new SearchCriteria.Builder<>(em, clazz);
    }
}
