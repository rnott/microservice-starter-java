## Project Structure

The project is implemented as a Maven multi-module project with the following modules

### /
Project parent.

### /api
Public API types, for example data transfer types.

### /service
REST service implementation.

### /acceptance
Acceptance (functional/feature) tests for the REST service.

*NOTE: if this module is named test archetype:create-from-project will fail to gather the project content (likely because test is also the value of a phase).*

### /container
Containerization (Docker) of the REST service. If you don't plan to use Docker you can remove the module from the parent POM. 

*NOTE: The standard Dockerfile is renamed Dockerfile.txt so that it will match the filtering fileset and be included in the achetype resources.*

