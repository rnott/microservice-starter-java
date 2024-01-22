#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2016 Randy Nott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ${package}.${artifactId}.model;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import ${package}.${artifactId}.util.Field;
import ${package}.${artifactId}.util.ClientException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class ExpansionRepositoryImpl <T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements ExpansionRepository<T, ID> {

	private final EntityManager entityManager;
	private final JpaEntityInformation<T, ?> entityInformation;

	public ExpansionRepositoryImpl( JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager ) {
		super( entityInformation, entityManager );
		this.entityInformation = entityInformation;
		this.entityManager = entityManager;
	}

	@Override
	public Optional<T> findById( ID id, List<Field> expansions ) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<T> query = builder.createQuery( getDomainClass() );
	    Root<T> root = query.from( getDomainClass() );
		join( root, expansions );
		entityInformation.getIdAttribute();
        query.select( root ).where( builder.equal( root.get( entityInformation.getIdAttribute() ), id ) );
        return Optional.of( entityManager.createQuery( query ).getSingleResult() );
	}

	@Override
	public List<T> findAll( List<Field> expansions ) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<T> query = builder.createQuery( getDomainClass() );
	    Root<T> root = query.from( getDomainClass() );
		join( root, expansions );
		query.select( root );
		return entityManager.createQuery( query ).getResultList();
	}

	private void join( Root<T> root, List<Field> expansions ) {
        if ( expansions != null ) {
        	// join each field
    	    for ( Field field : expansions ) {
                try {
                    Attribute<?, ?> attr = root.getModel().getAttribute( field.name() );
                    Fetch<?,?> fetch = root.fetch( attr.getName(), JoinType.LEFT );
                    join( fetch, field.subfields() );
                } catch ( IllegalArgumentException e ) {
                    // specified attribute does not exist
                    throw new ClientException( "Expansion property does not exist: " + field );
                }
            }
        }
	}

    private void join( Fetch<?, ?> fetch, List<Field> fields ) throws ClientException {
        if ( fields != null ) {
            for ( Field field : fields ) {
                // add join criteria
                try {
                    Fetch<?, ?> f = fetch.fetch( field.name(), JoinType.LEFT );

                    // recursively process sub-fields
                    join( f, field.subfields() );

                } catch ( IllegalArgumentException e ) {
                    // specified attribute does not exist
                    throw new ClientException( "Expansion property does not exist: " + fetch.getAttribute().getName() + "." + field );
                }
            }
        }
    }
}
