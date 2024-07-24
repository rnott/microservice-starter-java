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

package org.rnott.example.persistence;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import jakarta.persistence.NoResultException;
import jakarta.ws.rs.ext.Provider;
import org.rnott.example.problems.ProblemDetailsMapper;
import org.springframework.stereotype.Component;

/**
 * Map result not found errors to the response. The response has status 404.
 */
@Provider
@Component
public class NoResultExceptionMapper extends ProblemDetailsMapper<NoResultException> {

    /**
     * Default constructor.
     */
    public NoResultExceptionMapper() {
        super(NOT_FOUND);
    }
}
