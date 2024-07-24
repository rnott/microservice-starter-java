package org.rnott.example.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

/**
 * Configure criteria (filters, sorting, etc) to use when searching
 * an entity collection.
 * <p>
 * Note: two queries are required to support searching:
 * <ol>
 *     <lli>a query to fetch a single page of results</lli>
 *     <lli>a query to count the full set of results irrespective of paging</lli>
 * </ol>
 * In older versions of Hibernate, there were ways to share the predicates
 * between the two queries (such as using a selection alias) but these
 * mechanisms no longer work. The result is that both queries need to be
 * maintained in parallel.
 *
 * @param <T> the entity type
 */
@Getter
public class SearchCriteria<T extends AbstractEntity> {

    public static final String WILDCARD = "%";

    public static class Builder<T extends AbstractEntity> {

        private final EntityManager em;
        private final CriteriaBuilder cb;
        private final EntityType<T> model;
        private final CriteriaQuery<T> resultCriteria;
        private final Root<T> resultRoot;
        private final CriteriaQuery<Long> countCriteria;
        private final Root<T> countRoot;
        private final List<Predicate> resultPredicates = new LinkedList<>();
        private final List<Predicate> countPredicates = new LinkedList<>();
        private final List<String> sorting = new LinkedList<>();

        private MapJoin<T, String, String> resultTags;
        private MapJoin<T, String, String> countTags;
        private boolean isDeleted = false;

        public Builder(EntityManager em, Class<T> clazz) {
            this.em = em;

            cb = em.getCriteriaBuilder();
            resultCriteria = cb.createQuery(clazz);
            resultRoot = resultCriteria.from(clazz);
            resultRoot.alias("searchRoot");

            countCriteria = cb.createQuery(Long.class);
            countRoot = countCriteria.from(clazz);
            countCriteria.select(cb.count(countRoot));

            Metamodel meta = em.getMetamodel();
            model = meta.entity(clazz);
        }

        private MapJoin<T, String, String> getResultTags() {
            // created on demand to only join tags when part of the criteria
            if (resultTags == null) {
                resultTags = resultRoot.joinMap("tags");
            }
            return resultTags;
        }

        private MapJoin<T, String, String> getCountTags() {
            // created on demand to only join tags when part of the criteria
            if (countTags == null) {
                countTags = countRoot.joinMap("tags");

            }
            return countTags;
        }

        /**
         * Scopes the search to soft-deleted entities only. By
         * default, searches are scoped the entities that have not
         * been soft-deleted.
         *
         * @return this builder
         */
        public Builder<T> onlyDeletedEntities() {
            this.isDeleted = true;
            return this;
        }

        public Builder<T> exactMatch(String property, Object value) {
            resultPredicates.add(
                    cb.equal(resultRoot.get(property), value)
            );
            countPredicates.add(
                    cb.equal(countRoot.get(property), value)
            );
            return this;
        }

        public <V> Builder<T> optionsMatch(String property, V... values) {
            In<Object> clause = cb.in(resultRoot.get(property));
            for (Object v : values) {
                clause.value(v);
            }
            resultPredicates.add(clause);

            clause = cb.in(countRoot.get(property));
            for (Object v : values) {
                clause.value(v);
            }
            countPredicates.add(clause);
            return this;
        }

        public Builder<T> partialMatch(String property, String value) {
            resultPredicates.add(
                    cb.like(resultRoot.get(property), value)
            );
            countPredicates.add(
                    cb.like(countRoot.get(property), value)
            );
            return this;
        }

        public <V extends Comparable<? super V>> Builder<T> rangeMatch(String property, V floor, V ceiling) {
            resultPredicates.add(
                    cb.between(resultRoot.get(property), floor, ceiling)
            );
            countPredicates.add(
                    cb.between(countRoot.get(property), floor, ceiling)
            );
            return this;
        }

        public Builder<T> orderAs(List<String> criteria) {
            this.sorting.clear();
            this.sorting.addAll(criteria);
            return this;
        }

        public Builder<T> tagPresent(String key) {
            resultPredicates.add(
                    cb.equal(getResultTags().key(), key)
            );
            countPredicates.add(
                    cb.equal(getCountTags().key(), key)
            );
            return this;
        }

        public Builder<T> tagExactMatch(String key, Object value) {
            var rTags = getResultTags();
            var cTags = getCountTags();
            resultPredicates.add(
                    cb.and(
                            cb.equal(rTags.key(), key),
                            cb.equal(rTags.value(), value)
                    )
            );
            countPredicates.add(
                    cb.and(
                            cb.equal(cTags.key(), key),
                            cb.equal(cTags.value(), value)
                    )
            );
            return this;
        }

        public Builder<T> tagPartialMatch(String key, String value) {
            var rTags = getResultTags();
            var cTags = getCountTags();
            resultPredicates.add(
                    cb.and(
                            cb.equal(rTags.key(), key),
                            cb.like(rTags.value(), value)
                    )
            );
            countPredicates.add(
                    cb.and(
                            cb.equal(cTags.key(), key),
                            cb.like(cTags.value(), value)
                    )
            );
            return this;
        }

        public Builder<T> tagOptionsMatch(String key, Object... values) {
            var rTags = getResultTags();
            var cTags = getCountTags();
            In<Object> in = cb.in(rTags.value());
            for (Object v : values) {
                in.value(v);
            }
            resultPredicates.add(
                    cb.and(
                            cb.equal(rTags.key(), key),
                            in
                    )
            );
            in = cb.in(cTags.value());
            for (Object v : values) {
                in.value(v);
            }
            countPredicates.add(
                    cb.and(
                            cb.equal(cTags.key(), key),
                            in
                    )
            );
            return this;
        }

        public Builder<T> tagRangeMatch(String key, String floor, String ceiling) {
            var rTags = getResultTags();
            var cTags = getCountTags();
            resultPredicates.add(
                    cb.and(
                            cb.equal(rTags.key(), key),
                            //cb.between(rTags.get("value"), floor, ceiling),
                            cb.between(rTags.value(), floor, ceiling)
                    )
            );
            countPredicates.add(
                    cb.and(
                            cb.equal(cTags.key(), key),
                            cb.between(cTags.value(), floor, ceiling)
                    )
            );
            return this;
        }

        public SearchCriteria<T> build() {
            // apply deleted item scope
            exactMatch("deleted", isDeleted);

            // apply sorting
            Order[] orders = this.sorting.stream()
                    .map(s -> {
                        if (s.startsWith("+")) {
                            return cb.asc(resultRoot.get(s.substring(1)));
                        } else if (s.startsWith("-")) {
                            return cb.desc(resultRoot.get(s.substring(1)));
                        } else {
                            return cb.asc(resultRoot.get(s));
                        }

                    })
                    .toArray(Order[]::new);
            resultCriteria.orderBy(orders);

            TypedQuery<T> rq = em.createQuery(
                    resultCriteria.where(resultPredicates.toArray(Predicate[]::new))
            );

            TypedQuery<Long> cq = em.createQuery(
                    countCriteria.where(countPredicates.toArray(Predicate[]::new))
            );

            return new SearchCriteria<>(rq, cq);
        }
    }

    private final TypedQuery<T> resultsQuery;
    private final TypedQuery<Long> countQuery;

    private SearchCriteria(
            TypedQuery<T> resultsQuery,
            TypedQuery<Long> countQuery
    ) {
        this.resultsQuery = resultsQuery;
        this.countQuery = countQuery;
    }
}
