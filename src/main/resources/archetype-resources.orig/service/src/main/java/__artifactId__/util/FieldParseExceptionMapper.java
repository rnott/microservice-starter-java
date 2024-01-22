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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.ext.Provider;
import ${package}.${artifactId}.api.ErrorDetail;
import org.springframework.stereotype.Component;

/**
 * Map field specification errors to the response. The response has status 400.
 */
@Provider
@Component
public class FieldParseExceptionMapper extends AbstractExceptionMapper<FieldParseException> {

	/**
	 * Default constructor.
	 */
	public FieldParseExceptionMapper() {
		super( BAD_REQUEST );
	}

	@Override
	protected List<ErrorDetail> register( FieldParseException e ) {
		return Collections.singletonList( new ErrorDetail( e.getMessage() ) );
	}
}
