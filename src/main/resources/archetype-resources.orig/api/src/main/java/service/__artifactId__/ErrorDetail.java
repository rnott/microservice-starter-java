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

package ${package}.service.${artifactId};

/**
 * Error response detail entry. An error response is typically composed
 * of a list of these instances.
 */
public class ErrorDetail {

	public final String property;
	public final String message;

	/**
	 * Construct a detail instance with the specified error message.
	 * <p>
	 * @param message the error message.
	 */
	public ErrorDetail( final String message ) {
		this( message, null );
	}

	/**
	 * Construct a detail instance with the specified error message and
	 * property tag.
	 * <p>
	 * @param message the error message.
	 * @param property the property tag.
	 */
	public ErrorDetail( final String message, final String property ) {
		this.property = property;
		this.message = message;
	}
}
