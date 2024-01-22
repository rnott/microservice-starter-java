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

package ${package}.${artifactId}.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.spi.PersistentCollection;

/**
 * Mediation between an entity and binding (over the wire) formats.
 * <p>
 * @param <E> entity type.
 * @param <B> binding type.
 */
public interface Mediator <E, B> {

    /**
     * Covert an entity to its over the wire representation.
     * <p>
     * @param entity the entity to convert.
     * @return the over the wire representation.
     */
    B toBinding( E entity );

    /**
     * Convert an over the wire representation to an entity.
     * <p>
     * @param binding over the wire representation.
     * @return resulting entity.
     */
    E toEntity( B binding );

    /**
     * Convert an over the wire representation to an entity.
     * <p>
     * @param binding over the wire representation.
     * @param entity the target of the conversion.
     * @return the target entity.
     */
    E toEntity( B binding, E entity );

    /**
     * Remove entity manager proxies to avoid lazy loading exceptions. The
     * implementation is Hibernate specific and therefore not portable.
     * <p>
     * @param proxy the proxied entity.
     * @return the "unproxied" entity.
     */
    @SuppressWarnings("unchecked")
	default <T> T unproxy( T proxy ) {
    	if ( proxy instanceof PersistentCollection ) {
    		PersistentCollection collection = (PersistentCollection) proxy;
    		if ( collection instanceof PersistentSet ) {
    			Map<?, ?> entries = (Map<?, ?>) collection.getStoredSnapshot();
    			//Collection<? extends T> entries = (Collection<? extends T>) collection.getStoredSnapshot();
    			if ( entries == null ) {
    				return null;
    			}
    			return (T) new LinkedHashSet<T>( (Collection<? extends T>) entries.values() );
    		}
			return (T) collection.getStoredSnapshot();
		}
        return (T) Hibernate.unproxy( proxy );
    }
}
