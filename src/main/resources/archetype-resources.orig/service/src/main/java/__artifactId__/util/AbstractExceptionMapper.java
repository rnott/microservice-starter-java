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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import ${package}.${artifactId}.api.ErrorDetail;
import org.slf4j.LoggerFactory;

/**
 * Base class for implementing expection specific mappers.
 * <p>
 * @param <T> the throwable type.
 */
public abstract class AbstractExceptionMapper <T extends Throwable> implements ExceptionMapper<T> {

	private final Status status;
	private final MediaType type;

	/**
	 * Constructs an exception response with the specified HTTP status.
	 * <p>
	 * @param status the HTTP status for the response.
	 */
	public AbstractExceptionMapper( final Status status ) {
		this( status, APPLICATION_JSON_TYPE );
	}

	/**
	 * Constructs an exception response with the specified HTTP status and
	 * content type.
	 * <p>
	 * @param status the HTTP status for the response.
	 * @param type the response content type.
	 */
	protected AbstractExceptionMapper( final Status status, final MediaType type ) {
		this.status = status;
		this.type = type;
	}

	/**
	 * Register error detail entries for the given throwable.
	 * <p>
	 * @param throwable the throwable to process.
	 * @return the list of error detail entries generated from the throwable.
	 */
	abstract protected List<ErrorDetail> register( final T throwable );

	@Override
	public Response toResponse( T throwable ) {
		// log server errors
		if ( status.getStatusCode() >= 500 ) {
			LoggerFactory
				.getLogger( getClass() )
				.error( "", throwable );
		}
        return Response
			.status( status )
			.type( type )
			.entity( register( throwable ) )
			.build();
	}
}
