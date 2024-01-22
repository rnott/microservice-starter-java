package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class BadRequestExceptionMapper extends ProblemDetailsMapper<BadRequestException> {
    public BadRequestExceptionMapper() {
        super(Status.BAD_REQUEST);
    }
}
