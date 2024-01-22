package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class ForbiddenExceptionMapper extends ProblemDetailsMapper<ForbiddenException> {
    public ForbiddenExceptionMapper() {
        super(Status.FORBIDDEN);
    }
}
