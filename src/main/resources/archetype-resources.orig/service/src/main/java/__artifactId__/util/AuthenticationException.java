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

/**
 * Thrown to indicate an issue attempting the authenticate
 * a client or user, for example, a login attempt.
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = -1678144856638880436L;

    /**
     * Default constructor.
     */
    public AuthenticationException() {
        super();
    }

    /**
     * Construct the exception with the specified message text.
     * <p>
     * @param message the message text.
     */
    public AuthenticationException( final String message ) {
        super( message );
    }
}
