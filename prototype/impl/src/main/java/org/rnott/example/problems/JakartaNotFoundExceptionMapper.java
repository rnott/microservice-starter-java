package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class JakartaNotFoundExceptionMapper extends ProblemDetailsMapper<jakarta.ws.rs.NotFoundException> {
    public JakartaNotFoundExceptionMapper() {
        super(Status.NOT_FOUND);
    }
}
