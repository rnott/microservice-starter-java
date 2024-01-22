# Microservice Starter for Java
!!! This is a Work In Progress and not yet suitable for production use !!!
This project provides an archetypal and opinionated approach to Java/Sptring based microservice development 
including best practices, useful features, a modern technology stack and starters (or stubs) for implementing 
common use cases. Its use provides fast ramp up and helps ensure that all your new microservices conform to 
the same structure, patterns and architectural principals from the beginning.

As the name implies, this is meant as a starting point for new microservice development and you are encouraged 
to modify the reference service to meet your individual set of requirements. The reference service is functional,
meaning that you can edit, build and test your changes in place. Once you are satisfied with your changes, you
can publish the project and use the artifact it contains to start new microservice projects.

## Microservice Features

### API First microservice design
API-first design is an approach in software development where the design of the application starts with the 
definition and development of the application programming interface (API) before the implementation of the 
actual application. Among the many benefits to this approach are:
- Cross-platform compatibility across different platforms and devices
- Collaborative teams (such as BE and FE devs) can can work concurrently by agreeing on the API contract, thereby speeding up the overall development process
- Tooling support for code generation and documentation

### REST API
The service provides as an HTTP REST interface based on REST conventions.

### Search
Powerful search capabilities are provided to query data including complex filtering criteria, sorting, and result paging.

### Data tagging
The services offers support for data tagging. Tagging data can offer several benefits, enhancing the 
flexibility, organization, and query capabilities of persisted data. It provides a flexible way to associate 
additional metadata with existing data without requiring schema changes. Tags improve search and retrieval 
capabilities as users can tag data with relevant keywords or labels, making it easier to search for and 
retrieve specific information. Here are just some of the use cases that tagging enables:
- Enhanced search and retrieval
- User defined taxonomies
- Rapid prototyping
- Multi-dimensional classification
- Tagging standards compliance
- Data enrichment

#### Standardized error responses
All service endpoints provide structured error responses that are both human and machine-readable. The 
structure conforms to [RFC 9457: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html) 
as well as a number of handlers are included to map various service exceptions to this format.

### Soft-deletion
Soft delete is a technique used in databases where records are not permanently removed when they are deleted 
but are instead flagged as deleted. Such items do not normally interact with service functionality and thus 
appear as effectively deleted from the perspective of a client.  Soft delete can help to facilitate:
- Reversals (undo)
- Data recovery
- Error mitigation
- Audit trail and compliance
- Archiving inactive data

### Conflict detection
Conflicts can arise when two or more clients (users, processes, etc) are concurrently modifying the same 
data instance. In a common scenario, User A commits their changes then User B commits and the changes made 
by User A are lost since User B had no kknowledge they had been made. The service addresses this issue by 
using versioning to detect when this occurs, blocks all such updates and responsds with a specific error 
code indicating the conflict. Currently, resolution is left to the client. An alternative would be to have 
the service merge all changes so that none are lost.

### Integrated observability
Providing insights into what your service is performing as well as how it is performing is critical for
microservices and is integrated into the service, providing metrics for service calls as well as tracing major
service components, including database calls.

### Security
Foundational security is integrated out of the box to protect service endpoints from unauthorized use.

Builds use a combination of static analysis, automated testing and the OWASP critical vulnerability database 
to help guard against deployment of potentially vulnerable software.

### Additional Endpoints
Additional endpoints are provided to help you monitor and manage your service such as health check.

### Containerization
The project provides the option to deploy your service as a container.

### Test automation
Unit, integration and acceptance test automation is available support a modern CI/CD process. Integration
and acceptance test use actual dependencies such as PostgreSQL and Kafka launched as containers to allow 
testing against the actual providers that the service makes use of rather than mocks or shared resources.
This allows configurations to be tested as well as providing isolation and control of the test environment.

### Strict dependency management
- plugin and dependency versions are explicitly defined to support repeatable builds
- Dependencies are checked for known critical vulnerabilities using the OWASP database

## Technology Stack

The reference service makes use of the following frameworks and libraries:

- Java 21 (compatible)
- Maven (version 3.6.3+)
- SpringBoot (application container)
- Undertow (web container)
- Jersey (JAX-RS)
- Spring Data (data persistence)
- Hibernate (relational data)
- OpenAPI (service specification)
- OpenTelemetry (observability)
- Spring Security (authorization)
- MapStruct (data mapping)
- Flyway (data migration)
- Docker compose (containerization)
- JUnit 5 (unit and integration testing)
- TestContainers (test dependencies)
- Jacoco (test coverage)

## Project Structure
The project is a Maven archetype meant to initialize a prototypical microservice. It contains a working
version of the service prototype for this purpose. When the project builds, it dynamically transforms the
prototype into the structure required by the Maven archetype functionality. 
### src
This is the root of the Maven archetype.
### src/main/resources/META-INF/maven/archetype-metadata.xml
This is the Maven archetype descriptor. It describes the actions that Maven will take to process the archetype
resources when generating a new project.
### src/main/resources/archetype-resources
This is the root directory for the archetype resources. When the archetype itself is built, this directory is
dynamically populated from the service prototype.
### prototype
This is the microservice prototype and contains all source code. The prototype is structured as a Maven
multi-module sub-project and is fully functional. The prototype can be edited, built, tested and executed
independently of the archetype.
### prototype/api
This module contains the service definition file ```prototype/api/src/main/resources/openapi.yml``` used as 
the service contract. Build plugins use this definition to generate source code conforming to the 
specification. The source code consists of the service interface and data types. You should not need to edit
any files orhter than the service descriptor in this module.
### prototype/service
This module contains the service implementation and features. This module can be modified in any manner you
require to meet your specific requirements.
### prototype/container
This module is used to package the service application within a Docker container. The container can then
be published to the registry of your choice

## Usage
You can clone the project and use it without customization but in most cases you would first clone this project to your own repository and then make any desired changes. When finished, you will build and publish the project to your own artifact repository where you pull dependencies from. Once completed, you can use the architype to create new microservice projects based off your cloned project. 
1. Change to the directory you wish the project to be created in
2. Run the Maven architype to create the new project:
```
$ mvn archetype:generate -DarchetypeGroupId=org.rnott.samples.microservice \
-DarchetypeArtifactId=microservice-archetype -DarchetypeVersion=1.0.0.Final \
-DgroupId=<your-service-groupId> -DartifactId=<your-service-artifactId>
```
*NOTE*: you will likely want to change the groud and artifact identifiers to align with you organization. You will need to provide those as the architype settings shown above.

## Verifying the new project

In the newly created project directory 
```
$ mvn clean verify
```
The project should build and all tests should pass.

## Runing the new project
Use the following command to run the new project:
    $ java -jar service/target/service-<version>-bin.jar options
    
    where:  
        --server.port=####		override the default server listener port of 8080
Make sure to replace <version> with your build version, for example 1.0.0.Final.
The service will fail until you configure the data repository information in ```src/main/respources/application.yml```.
