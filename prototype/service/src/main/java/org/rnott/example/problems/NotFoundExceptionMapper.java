package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class NotFoundExceptionMapper extends ProblemDetailsMapper<NotFoundException> {
    public NotFoundExceptionMapper() {
        super(Status.NOT_FOUND);
    }
}
