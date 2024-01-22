package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class NotAuthorizedExceptionMapper extends ProblemDetailsMapper<NotAuthorizedException> {
    public NotAuthorizedExceptionMapper() {
        super(Status.UNAUTHORIZED);
    }
}
