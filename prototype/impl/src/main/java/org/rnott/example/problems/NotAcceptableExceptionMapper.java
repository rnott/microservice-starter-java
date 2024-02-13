package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class NotAcceptableExceptionMapper extends ProblemDetailsMapper<NotAcceptableException> {
    public NotAcceptableExceptionMapper() {
        super(Status.NOT_ACCEPTABLE);
    }
}
