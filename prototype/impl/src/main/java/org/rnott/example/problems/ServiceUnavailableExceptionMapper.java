package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class ServiceUnavailableExceptionMapper extends ProblemDetailsMapper<ServiceUnavailableException> {
    public ServiceUnavailableExceptionMapper() {
        super(Status.SERVICE_UNAVAILABLE);
    }
}
