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

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import org.rnott.example.api.ErrorItem;
import org.rnott.example.api.ProblemDetails;
import org.springframework.stereotype.Component;

/**
 * Map constraint violations to the response. The response has status 400.
 */
@Provider
@Component
public class ConstraintViolationExceptionMapper extends ProblemDetailsMapper<ConstraintViolationException> {

    /**
     * Default constructor.
     */
    public ConstraintViolationExceptionMapper() {
        super(BAD_REQUEST);
    }

    @Override
    protected ProblemDetails problem(ConstraintViolationException e) {
        return super.problem(e)  // let superclass handle the common settings
                .errors(
                        e.getConstraintViolations().stream()
                                .map(cv -> {
                                    return new ErrorItem()
                                            .detail(cv.getMessage())
                                            .pointer(toJsonPointer(cv.getPropertyPath()))
                                            .extensions(Map.of("value", cv.getInvalidValue()));
                                })
                                .toList()
                );
    }

    private String toJsonPointer(Path path) {
        final StringBuilder pointer = new StringBuilder("#");
        path.forEach(n -> {
            if (n.getKind().equals(ElementKind.PROPERTY)) {
                pointer.append("/").append(n.getName());
                if (n.isInIterable()) {
                    if (n.getIndex() != null) {
                        // collection index
                        pointer.append("/").append(n.getIndex());
                    } else if (n.getKey() != null) {
                        // map key
                        pointer.append("/").append(n.getKey());
                    }
                }
            }
        });
        return pointer.toString();
    }
}
