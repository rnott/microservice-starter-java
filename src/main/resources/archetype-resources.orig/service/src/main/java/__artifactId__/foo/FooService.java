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

package ${package}.${artifactId}.foo;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import ${package}.${artifactId}.util.Field;
import org.springframework.stereotype.Service;

@Service
public class FooService {

	@Inject
	private FooRepository repository;

    public Set<FooEntity> fetchAll() {
    	return fetchAll( null );
    }

    public Set<FooEntity> fetchAll( List<Field> expansions ) {
    	return new HashSet<FooEntity>( repository.findAll() );
    }

    public FooEntity fetch( String id ) {
    	return fetch( id, null );
    }

    public FooEntity fetch( String id, List<Field> expansions ) {
    	Optional<FooEntity> foo = repository.findById( id, expansions );
    	if ( foo.isPresent() ) {
    		return foo.get();
    	}
    	throw new NoResultException( "ID not found: " + id );
    }

    public FooEntity create( FooEntity foo ) {
    	foo.setId( UUID.randomUUID().toString() );
    	return repository.save( foo );
    }

    public FooEntity update( FooEntity foo ) {
    	return repository.save( foo );
    }
}
