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

import ${package}.${artifactId}.api.Bar;
import ${package}.${artifactId}.util.Mediator;

public class BarMediator implements Mediator<BarEntity, Bar> {

	@Override
	public Bar toBinding( BarEntity entity ) {
		// prevent lazy loading exceptions
		entity = unproxy( entity );

		Bar bar = new Bar();
		bar.id = entity.getId();
		bar.name = entity.getName();
		return bar;
	}

	@Override
	public BarEntity toEntity( Bar binding ) {
		return toEntity( binding, new BarEntity() );
	}

	@Override
	public BarEntity toEntity( Bar binding, BarEntity entity ) {
		entity.setId( binding.id );
		entity.setName( binding.name );
		return entity;
	}
}
