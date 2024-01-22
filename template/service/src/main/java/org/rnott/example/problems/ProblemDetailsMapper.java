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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.rnott.example.api.ProblemDetails;
import org.slf4j.LoggerFactory;

/**
 * Base class for implementing exception specific mappers that serialize
 * exceptions as RFC 9457 Problem Details content, which is both numan and
 * machine readable.
 * <p>
 * @param <E> the throwable type
 */
public abstract class ProblemDetailsMapper <E extends Throwable> implements ExceptionMapper<E> {

	/*
	Binding problem types and tiles to HTTP status codes by default.
	Since the problems registry is not well-used, the HTTP specification is used
	as the registry for binding HTTP status codes.
	 */
	private static final Map<Integer, String> TYPES = new LinkedHashMap<>();
	private static final Map<Integer, String> TITLES = new LinkedHashMap<>();
	static {
		TYPES.put(400, "https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request");
		TITLES.put(400, "Bad Request");
		TYPES.put(401, "https://www.rfc-editor.org/rfc/rfc9110.html#name-401-unauthorized");
		TITLES.put(401, "Unauthorized");
		TYPES.put(402,"https://www.rfc-editor.org/rfc/rfc9110.html#name-402-payment-required");
		TITLES.put(402, "Payment Required");
		TYPES.put(403, "https://www.rfc-editor.org/rfc/rfc9110.html#name-403-forbidden");
		TITLES.put(403, "Forbidden");
		TYPES.put(404, "https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found");
		TITLES.put(404, "Not Found");
		TYPES.put(405, "https://www.rfc-editor.org/rfc/rfc9110.html#name-405-method-not-allowed");
		TITLES.put(405, "Method Not Allowed");
		TYPES.put(406, "https://www.rfc-editor.org/rfc/rfc9110.html#name-406-not-acceptable");
		TITLES.put(406, "Not Acceptable");
		TYPES.put(407, "https://www.rfc-editor.org/rfc/rfc9110.html#name-407-proxy-authentication-re");
		TITLES.put(407, "Proxy Authentication Required");
		TYPES.put(408, "https://www.rfc-editor.org/rfc/rfc9110.html#name-408-request-timeout");
		TITLES.put(408, "Request Timeout");
		TYPES.put(409, "https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict");
		TITLES.put(409, "Conflict");
		TYPES.put(410, "https://www.rfc-editor.org/rfc/rfc9110.html#name-410-gone");
		TITLES.put(410, "Gone");
		TYPES.put(411, "https://www.rfc-editor.org/rfc/rfc9110.html#name-411-length-required");
		TITLES.put(411, "Length Required");
		TYPES.put(412, "https://www.rfc-editor.org/rfc/rfc9110.html#name-412-precondition-failed");
		TITLES.put(412, "Precondition Failed");
		TYPES.put(413, "https://www.rfc-editor.org/rfc/rfc9110.html#name-413-content-too-large");
		TITLES.put(413, "Content Too Large");
		TYPES.put(414, "https://www.rfc-editor.org/rfc/rfc9110.html#name-414-uri-too-long");
		TITLES.put(4114, "URL Too Long");
		TYPES.put(415, "https://www.rfc-editor.org/rfc/rfc9110.html#name-415-unsupported-media-type");
		TITLES.put(415, "Unsupported Media Type");
		TYPES.put(416, "https://www.rfc-editor.org/rfc/rfc9110.html#name-416-range-not-satisfiable");
		TITLES.put(416, "Range Not Satisfied");
		TYPES.put(417, "https://www.rfc-editor.org/rfc/rfc9110.html#name-417-expectation-failed");
		TITLES.put(417, "Expectation Failed");
		TYPES.put(418, "https://www.rfc-editor.org/rfc/rfc9110.html#name-418-unused");
		TITLES.put(418, "(Unused)");
		TYPES.put(421, "https://www.rfc-editor.org/rfc/rfc9110.html#name-421-misdirected-request");
		TITLES.put(421, "Misdirected Request");
		TYPES.put(422, "https://www.rfc-editor.org/rfc/rfc9110.html#name-422-unprocessable-content");
		TITLES.put(422, "Unprocessable Content");
		TYPES.put(426, "https://www.rfc-editor.org/rfc/rfc9110.html#name-426-upgrade-required");
		TITLES.put(426, "Upgrade Required");
		TYPES.put(500, "https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error");
		TITLES.put(500, "Internal Server Error");
		TYPES.put(501, "https://www.rfc-editor.org/rfc/rfc9110.html#name-501-not-implemented");
		TITLES.put(501, "Not Implemented");
		TYPES.put(502, "https://www.rfc-editor.org/rfc/rfc9110.html#name-502-bad-gateway");
		TITLES.put(502, "Bad Gateway");
		TYPES.put(503, "https://www.rfc-editor.org/rfc/rfc9110.html#name-503-service-unavailable");
		TITLES.put(503, "Service Unavailable");
		TYPES.put(504, "https://www.rfc-editor.org/rfc/rfc9110.html#name-504-gateway-timeout");
		TITLES.put(504, "Gateway Timeout");
		TYPES.put(505, "https://www.rfc-editor.org/rfc/rfc9110.html#name-505-http-version-not-suppor");
		TITLES.put(505, "HTTP Version Not Supported");
	}

	protected static final String MEDIA_PROBLEM_DETAIL = "application/problem+json";

	protected final int status;

	/**
	 * Constructs an exception response with the specified HTTP status and a
	 * content type of plain text.
	 * <p>
	 * @param status the HTTP status for the response.
	 */
	protected ProblemDetailsMapper(Status status) {
		this(status.getStatusCode());
	}

	/**
	 * Constructs an exception response with the specified HTTP status and a
	 * content type of plain text.
	 * <p>
	 * @param status the HTTP status for the response.
	 */
	protected ProblemDetailsMapper(int status) {
		this.status = status;
	}

	/**
	 * Determine the problem type. By default, the type identifies
	 * the status code defined in the HTTP specification.
	 *
	 * @param status HTTP status code
	 * @return the corresponding type URI
	 */
	protected String getType(int status) {
		return TYPES.get(status);
	}

	/**
	 * Determine the problem title. By default, the title is the
	 * human-readable text defined for the status code by the HTTP specification.
	 *
	 * @param status HTTP status code
	 * @return the corresponding status text
	 */
	protected String getTitle(int status) {
		return TITLES.get(status);
	}

	/**
	 * Create a problem details instance for the given throwable. The
	 * instance can be customized by overriding this method.
	 * <p>
	 * @param throwable the throwable to process
	 * @return a problem details instance representing the throwable
	 */
	protected ProblemDetails problem( final E throwable ) {
		return new ProblemDetails()
				.type(getType(status))
				.title(getTitle(status))
				.status(status)
				.detail(throwable.getLocalizedMessage());
	}

	@Override
	public Response toResponse( E throwable ) {
		// TODO: to log or not to log?
		if ( status >= 500 ) {
			LoggerFactory.getLogger(getClass()).error( "Server exception", throwable );
		} else if (status >= 400) {
			LoggerFactory.getLogger(getClass()).warn("Client exception", throwable);
		}

		ProblemDetails problem = problem( throwable );
        return Response
			.status(status)
			.type(MEDIA_PROBLEM_DETAIL)
			.entity(problem)
			.build();
	}
}
