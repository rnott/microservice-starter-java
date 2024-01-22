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
import ${package}.${artifactId}.util.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Data repository providing on-demand eager loading of entity relationships.
 * This behavior can be applied recursively, e.g. A -> B -> C.
 * <p>
 * @param <T> entity type.
 * @param <ID> entity identifier type.
 * @see Field
 */
@NoRepositoryBean
public interface ExpansionRepository <T, ID extends Serializable> extends JpaRepository<T, ID> {

	/**
	 * Fetch an entity by identifier. Entity relationships can be eagerly
	 * loaded by specifying one or more expansions.
	 * <p>
	 * @param id the entity identifier.
	 * @param expansions one or more relationships to be eagerly loaded.
	 * @return the entity associated with the specified identifier.
	 */
	Optional<T> findById(ID id, List<Field> expansions );

	/**
	 * Fetch all entities. Entity relationships can be eagerly
	 * loaded by specifying one or more expansions.
	 * <p>
	 * @param expansionsexpansions one or more relationships to be eagerly loaded.
	 * @return the list of entities.
	 */
	List<T> findAll( List<Field> expansions );
}
