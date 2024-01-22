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
import java.util.Collections;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import ${package}.${artifactId}.api.ErrorDetail;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Map an exception to the response. This implementation handles the following
 * use cases:
 * <ul>
 * <li>Wrapped exceptions - the exception is recursively unwrapped until a
 * specific handler for the exception can be found.
 * <li>Unmapped exceptions - if no specific mapper can be found for the causal
 * chain, a ${artifactId} error response (HTTP 500) is returned.
 * </ul> 
 */
@Provider
@Component
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    private Providers providers;

    private final MediaType type;

	/**
	 * Default constructor.
	 */
    public DefaultExceptionMapper() {
    	this( APPLICATION_JSON_TYPE );
    }

	/**
	 * Constructs an exception response with the specified HTTP status and
	 * content type.
	 * <p>
	 * @param status the HTTP status for the response.
	 * @param type the response content type.
	 */
    protected DefaultExceptionMapper( final MediaType type ) {
    	this.type = type;
    }

    @Override
    public Response toResponse( Throwable t ) {
        // use the best response in the cause chain
        Response response = buildResponse( t );
        if ( response != null ) {
            return response;
        }

		LoggerFactory
			.getLogger( getClass() )
			.error( "", t );

        // use response if it has been built by framework
        if ( t instanceof WebApplicationException ) {
            return ((WebApplicationException) t).getResponse();
        }

        // generic response
        return Response
        	.serverError()
        	.type( type )
        	.entity( Collections.singletonList( new ErrorDetail( t.getMessage() ) ) )
        	.build();
    }

    /**
     * Find the best handler for an exception. It processes the deepest cause mapped
     * to a handler.
     * <p>
     * @param t - unexpected exception thrown in the code.
     * @return response - the Response sent to the Client.
     */
    protected Response buildResponse( Throwable t ) {
        if ( t == null ) {
            // end of causal chain
            return null;
        }

        // try next in chain
        Response response = buildResponse( t.getCause() );

        // handled deeper
        if ( response != null ) {
            return response;
        }

        // attempt to handle
        @SuppressWarnings("unchecked")
        ExceptionMapper<Throwable> mapper = (ExceptionMapper<Throwable>)providers.getExceptionMapper( t.getClass() );
        if ( mapper != null && mapper.getClass() != this.getClass() ) { 
                return mapper.toResponse( t );
        }

        // can't handle
        return null;
    }
}
