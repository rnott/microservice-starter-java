package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class InternalServerErrorExceptionMapper extends ProblemDetailsMapper<InternalServerErrorException> {
    public InternalServerErrorExceptionMapper() {
        super(Status.INTERNAL_SERVER_ERROR);
    }
}
