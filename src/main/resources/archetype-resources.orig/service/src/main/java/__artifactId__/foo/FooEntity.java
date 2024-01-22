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

import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import ${package}.${artifactId}.model.AbstractEntity;

@Entity
@Table(name = "FOO")
@NamedQueries({
	@NamedQuery(name = "Foo.findAll", query = "SELECT foo FROM FooEntity foo")
})
public class FooEntity extends AbstractEntity {

	@Id
	private String id;

	@Column
	private String name;

	/*
	 * Used when not joining
	 */
	@Column(name = "ANOTHER_ID")
	private String anotherId;

	/*
	 * Used when joining (expanded)
	 */
	@OneToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH})
	@JoinColumn(name = "ANOTHER_ID", insertable = false, updatable = false)
	private FooEntity another;

	//@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "FOO_ID")
	private Set<BarEntity> bar;

	//@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "FOO_ID")
	private List<BarEntity> barList;

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getAnotherId() {
		return anotherId;
	}

	public void setAnotherId( String anotherId ) {
		this.anotherId = anotherId;
	}

	public FooEntity getAnother() {
		return another;
	}

	public void setAnother( FooEntity another ) {
		this.another = another;
	}

	public Set<BarEntity> getBar() {
		return bar;
	}

	public List<BarEntity> getBarList() {
		return barList;
	}

	public void setBar( Set<BarEntity> bar ) {
		this.bar = bar;
	}
}
