package org.rnott.example.problems;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.ws.rs.core.Response.Status;

public class JsonMappingExceptionMapper extends ProblemDetailsMapper<JsonMappingException> {
    public JsonMappingExceptionMapper() {
        super(Status.BAD_REQUEST);
    }
}
