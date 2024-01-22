package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;

public class NotSupportedExceptionMapper extends ProblemDetailsMapper<NotSupportedException> {
    public NotSupportedExceptionMapper() {
        super(Status.UNSUPPORTED_MEDIA_TYPE);
    }
}
