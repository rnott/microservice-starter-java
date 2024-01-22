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

package ${package}.maven.rest;

import ${package}.${artifactId}.util.Field;
import ${package}.${artifactId}.util.FieldParseException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test parsing of field specifications.
 * <p>
 * @see Field
 */
public class FieldTest {

	@DataProvider(name="valid")
	private Object [][] valid() {
		return new Object [][] {
			{ "one", 1 },
			{ "one23", 1 },
			{ "a2c", 1 },
			{ "one two three", 3 },
			{ "one  two  three ", 3 },
			{ " one  two  three", 3 },
			{ " one  two  three ", 3 },
			{ "a", 1 },
			{ "a b c", 3 },
			{ "ay b c", 3 },
			{ "a bee c", 3 },
			{ "a b see", 3 },
			{ "ay bee see", 3 },

			{ "one (two) three", 2 },
			{ "one ( two ) three", 2 },
			{ " one  (two ) three", 2 },
			{ " one ( two) three ", 2 },
			{ "one two(three)", 2 },
			{ "one  two ( three)", 2 },
			{ " one  two ( three )", 2 },
			{ " one  two ( three ) ", 2 },
			{ "one( two three)", 1 },
			{ "one ( two  three)", 1 },
			{ " one ( two  three )", 1 },
			{ " one ( two  three ) ", 1 },

			{ "one(two(three))", 1 },
			{ "one ( two(  three))", 1 },
			{ "one (two (three ) ) ", 1 },
			{ "one ( two ( three ) ) ", 1 },

			{ "one two((three))", 2 },
			{ "(one two three)", 3 }
		};
	}

	@DataProvider(name="invalid")
	private Object [][] invalid() {
		return new Object [][] {
			{ "1one" },
			{ "one_two" },
			{ "one/two" },
			{ "one,two" },
			{ "one (two three" },
			{ "one, two (three" },
			{ "one  two ) three " },
			{ "one  two  three)" },
			{ "one  (two  (three) " },
			{ "one  two  (three))" }
		};
	}

	@Test(dataProvider = "valid")
	public void fields( String s, int expected ) {
		Field f = Field.newInstance( s );
		assert f != null : "No fields";
		assert expected == f.subfields().size()
			: "Unexpected field count: " + f.subfields().size() + ", expected: " + expected;
		//f.subfields().forEach( System.out::println );
		//System.out.println();
	}


	@Test(dataProvider = "invalid", expectedExceptions = FieldParseException.class)
	public void errors( String s ) {
		Field.newInstance( s );
	}

	@Test
	public void subfields() {
		Field f = Field.newInstance( "a b c" );
		assert f.hasSubfields() : "Expected subfields";
		assert f.subfields().size() == 3 : "Unexpected subfield count: " + f.subfields().size();
		assert "a".contentEquals( f.subfields().get( 0 ).name() )
			: "Unexpected subfield: " + f.subfields().get( 0 ).name();
		assert "b".contentEquals( f.subfields().get( 1 ).name() )
		: "Unexpected subfield: " + f.subfields().get( 1 ).name();
		assert "c".contentEquals( f.subfields().get( 2 ).name() )
		: "Unexpected subfield: " + f.subfields().get( 2 ).name();
		f.subfields().forEach( sf -> {
			assert ! sf.hasSubfields() : "Unexpected subfields: " + sf.name();
		});
	}

	@Test
	public void toStringMethod() {
		Field f = Field.newInstance( "a b c" );
		assert "(a b c)".contentEquals( f.toString() )
			: "Unexpected toString(): " + f.toString();
		f = Field.newInstance( " a  ( b c ) " );
		assert "(a(b c))".contentEquals( f.toString() )
			: "Unexpected toString(): " + f.toString();
	}
}
