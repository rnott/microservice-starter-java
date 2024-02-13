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

package org.rnott.example.problems;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Map an exception to the response. This implementation handles the following
 * use cases:
 * <ul>
 * <li>Wrapped exceptions - the exception is recursively unwrapped until a
 * specific handler for the exception can be found.
 * <li>Unmapped exceptions - if no specific mapper can be found for the causal
 * chain, a service error response (HTTP 500) is returned.
 * </ul>
 */
@Provider
@Component
public class DefaultExceptionMapper extends ProblemDetailsMapper<Throwable> {

    @Context
    private Providers providers;

	/**
	 * Default constructor.
	 */
    public DefaultExceptionMapper() {
        super(Status.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response toResponse(Throwable t) {
        // use the best response in the cause chain
        Optional<Response> response = buildResponse(t);
        // punt to superclass if no handler found
        return response.orElseGet(() -> super.toResponse(t));
    }

    /**
     * Find the best handler for an exception. It processes the deepest cause mapped
     * to a handler.
     * <p>
     * @param t - unexpected exception thrown in the code.
     * @return response - the Response sent to the Client.
     */
    protected Optional<Response> buildResponse(Throwable t) {
        if (t == null) {
            // end of causal chain
            return Optional.empty();
        }

        // try next in chain
        Optional<Response> response = buildResponse(t.getCause());

        // handled deeper
        if (response.isPresent()) {
            return response;
        }

        // attempt to handle
        @SuppressWarnings("unchecked")
        ExceptionMapper<Throwable> mapper = (ExceptionMapper<Throwable>)providers.getExceptionMapper( t.getClass() );
        if (mapper != null && mapper.getClass() != this.getClass()) {
                Response r = mapper.toResponse(t);
                if (r == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(r);
                }
        }

        // can't handle
        return Optional.empty();
    }
}
