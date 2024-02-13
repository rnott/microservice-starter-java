## Project Structure

The project is implemented as a Maven multi-module project with the following modules

### /
Project root. Common settings are configured here.

### /api
The API specification. All source code in this module is generated from the specification.

### /service
REST service implementation. The executable JAR and optionally a containerized image of
the service are built here. Unit and a subset of functional tests are executed as part 
of the build. The functional tests to be executed for each build should include 
a minimal set of tests that act as a 'smoke test' for a code commit.

### /testing
Test suite for the REST service.

This module is executed post-build as part of a CI/CD pipeline and used
to test a deployed release candidate of the service. The tests ensure
the expected functioning of the service as well as non-functional
aspects such as performance and security.
