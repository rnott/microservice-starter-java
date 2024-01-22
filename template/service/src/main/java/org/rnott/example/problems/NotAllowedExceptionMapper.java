package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class NotAllowedExceptionMapper extends ProblemDetailsMapper<NotAllowedException> {
    public NotAllowedExceptionMapper() {
        super(Status.METHOD_NOT_ALLOWED);
    }
}
