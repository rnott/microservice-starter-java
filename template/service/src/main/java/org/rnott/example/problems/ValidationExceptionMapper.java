package org.rnott.example.problems;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response.Status;

public class ValidationExceptionMapper extends ProblemDetailsMapper<ValidationException> {
    public ValidationExceptionMapper() {
        super(Status.BAD_REQUEST);
    }
}
