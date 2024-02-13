package org.rnott.example.problems;

import com.fasterxml.jackson.core.JsonParseException;
import jakarta.ws.rs.core.Response.Status;

public class JsonParseExceptionMapper extends ProblemDetailsMapper<JsonParseException> {
    public JsonParseExceptionMapper() {
        super(Status.BAD_REQUEST);
    }
}
