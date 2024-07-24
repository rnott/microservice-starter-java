package org.rnott.example.problems;

import jakarta.ws.rs.core.Response.Status;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

public class ObjectOptimisticLockingFailureExceptionMapper extends
        ProblemDetailsMapper<ObjectOptimisticLockingFailureException> {
    public ObjectOptimisticLockingFailureExceptionMapper() {
        super(Status.CONFLICT);
    }
}
