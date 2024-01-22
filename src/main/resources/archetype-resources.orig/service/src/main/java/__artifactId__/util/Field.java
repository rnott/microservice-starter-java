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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Specification that can be used to declare desired activities on
 * relationships between types. The primary use of this class is to
 * describe which relationships should be eagerly loaded during a query.
 */
public class Field {

	private final String name;
	private final List<Field> subfields = new ArrayList<>();

	private Field( final String name ) {
		this.name = name;
	}

	/**
	 * Determine the field name.
	 * <p>
	 * @return the field name.
	 */
	public String name() { return name; }

	/**
	 * Determine if subfields were specified for the field.
	 * <p>
	 * @return <code>true</true> if subfields were specified,
	 * <code>false</code> otherwise.
	 */
	public boolean hasSubfields() { return subfields.size() > 0; }

	/**
	 * Determine the subfields specified for the field.
	 * <p>
	 * @return the list of specified subfields.
	 */
	public List<Field> subfields() {
		return Collections.unmodifiableList( subfields );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( name );
		if ( subfields.size() > 0 ) {
			sb.append( '(' );
			subfields.forEach( sf -> {
				sb.append(sf.toString() ).append( ' ' );
			});
			sb.setCharAt( sb.length() - 1, ')' );
		}

		return sb.toString();
	}

	private Field add( final Field f ) {
		subfields.add( f );
		return this;
	}

	/**
	 * Create a new instance that represents the provided specification.
	 * <p>
	 * Fields are instance property names separated by spaces. A field can
	 * specify subfields by enclosing those fields in parenthesis.
	 * <h3>Examples</h3><ul>
	 * <li>A B C - specifies properties A, B and C of an instance
	 * <li>A (B C) - specifies property A of an instance and properties
	 * B and C of property A
	 * <li> A (B (C)) - specifies property A of an instance, property
	 * B of property A and property C of property B.
	 * </ul>
	 * @param specification the field specification.
	 * @return a new instance that represents the provided specification.
	 */
	public static Field newInstance( final String specification ) {
		Stack<Field> subfields = new Stack<Field>();

		// root
		subfields.push( new Field( "" ) );

		int start = 0, pos = 0;
		Field f = null;

		while ( pos < specification.length() ) {
			Character c = specification.charAt( pos );
			if ( Character.isAlphabetic( c ) ) {
				pos++;

			} else if ( Character.isDigit( c ) && start != pos ) {
				// digit okay except for first character
				pos++;

			} else {
				if ( pos > start ) {
					// end property name
					f = new Field( specification.substring( start, pos ) );
					subfields.peek().add( f );
					pos++;
					start = pos;
				} else {
					pos++; start++;
				}

				if ( c == ' ' ) {
					// ignore

				} else if ( c == '(' ) {
					// begin sub properties
					if ( f != null ) {
						subfields.push( f );
						f = null;
					} else {
						// dummy field so parenthesis balance
						subfields.push( new Field( "__dummy" ) );
					}

				} else if ( c == ')' ) {
					// end sub properties
					if ( subfields.size() > 1 ) {
						Field tmp = subfields.pop();

						// handle special case (a b c)
						if ( "__dummy".equals( tmp.name) ) {
							// transfer fields to the parent
							subfields.get( subfields.size() - 1 )
								.subfields.addAll( tmp.subfields );
						}
					} else {
						throw new FieldParseException( "Missmatched parenthesis " + c + " at position " + pos );
					}
				} else {
					throw new FieldParseException( "Unexpected character " + c + " at position " + pos );
				}
			}
		}

		// complete final field if necessary
		if ( pos > start ) {
			f = new Field( specification.substring( start, pos ) );
			subfields.peek().add( f );
		}

		if ( subfields.size() == 1 ) {
			return subfields.pop();
		} else {
			throw new FieldParseException( "Unexpected EOL at position " + pos );
		}
	}
}
