# Microservice Starter for Java
An archetypal, opinionated approach to microservice development including best practiones, useful features, a technology stack and recipies for implementing common use cases. Its use helps ensure that all your new microservices conform to the same structure, patterns and architectural principals from the beginning.

As the name implies, this is meant as a starting point for new microservice development and you are encouraged to modify the source to meet your individual set of requirements.

This is a multiple module project. The purpose of each module is explained below:
* Root moudle - contains any definitions common to the project
* maven-architype - a Maven architype that creates new project instances based on the companion service code
* service - the service source code used to populate new project instances. This can be customized to match your requirements in any way you see fit

## Microservice Features

### API First microservice design
API-first design is an approach in software development where the design of the application starts with the definition and development of the application programming interface (API) before the implementation of the actual application. Among the many benefits to this approach are:
- Cross-platform compatability across different platforms and devices
- Collaborative teams (such as BE and FE devs) can can work concurrently by agreeing on the API contract, thereby speeding up the overall development process
- Tooling support for code generation and documentation

### REST API
An HTTP based REST interface is provided
#### Search
Powerful search cababilities are provided to query data including complex filtering criteria, sorting, and result paging.
#### Metadata
Separate endpoints are provided for viewing and management of entity metadata, such as tags.
#### Standardized error responses
All service endpoints provide structured error responses when an issue is encountered. The structure conforms to RFC xxx Problem Details and a number of handlers are included to map various service exceptions to this format.

### Entity soft-deletion
Soft delete is a technique used in databases where records are not permanently removed when they are deleted but are instead flagged as deleted. Such items do not normally interact with service functionality and thus appear as effectively deleted from the perspective a a client.  Soft delete can help to facilitate:
- Reversability (undo)
- Data recovery
- Error mitigation
- Audit trail and compliance
- Archiving inactive data

### Entity tagging
Tagging entity data can offer several benefits, enhancing the flexibility, organization, and query capabilities of persisted data. They provide a flexible way to associate additional metadata with existing data without requiring schema changes. Tags can improve search and retrieval capabilities as users can tag data entities with relevant keywords or labels, making it easier to search for and retrieve specific information.
#### Use cases
- Enhanced search and retrieval
- User defined taxonomies
- Rapid prototyping
- Mulit-dimensional classification
- Tagging standanrds compliance
- Data enrichment

### Conflict detection
Conflicts can arise when two or more clients (users, processes, etc) are concurrently modifying the same data instance. In a common scenario, User A commits their changes then User B commits and the changes made by User A are lost since User B had no kknowledge they had been made. The service addresses this issue by using versioning to detect when this occurs, blocks all such updates and responsds with a specific error code indicating the conflict. Currently, resolution is left to the client. An alternative would be to have the service merge all changes so that none are lost.

### Automated observability
Observability in software development refers to the ability to gain insights into the internal workings of a system by collecting, analyzing, and visualizing data. The service configures 

### Automated event publication

### Containerization

### Zero Trust

### Test automation

### Strict dependency management
- plugin and dependency versions are explicitly defined to support repeatable builds
- Dependencies are checked for known critical vulnerabilities using the OWASP database


## Techonology Stack

The generated service makes use of the following frameworks and libraries:

- Java 21 (compatible)
- Maven (version 3.6.3+)
- SpringBoot (application container)
- Undertow (web container)
- Jersey (JAX-RS)
- SpringData
-- Hibernate (relational data)
- OpenAPI
- OpenTelemetry
- OpenID Connect
- MapStruct (data mapping)
- Flyway (data migration)
- Docker componse (service containerization)
- JUnit 5 (unit and integration testing)
- Jacoco (test coverage)

## Features

- Java 11 source compatibility
- Spring runtime environment
- Executable JAR file format
- Standard acuators enabled
- Undertow web tier
- Standards based implementations
    - JAX-RS (Jersey)
    - JPA (Hibernate)
- Pragmatic resource expansion capabilities (see below)
- Standardized mapping of exceptions to responses
- Feature/functional based acceptance testing supporting full automation
- Code coverage reports
- Additional quality plugins
    - OWASP exploit reporting on dependencies
    - dependency versioning reporting

### Resource expansion

Of special note is the ability to eagerly join lazy associations of resource entities. This pragmatic approach allows a client to minimize the number of service calls required to obtain a full object graph. For example, given an entity relationship of A -> B -> C, it is possible to request /path/A?expand=B(C) and obtain a response containing the full objedt graph.

## Caching

This project **does not** implement nor enable caching. Instead, it is proposed that one should make use of web cache (web server, web proxy, load balancer, etc) as it will almost certainly be more effience and performant than any service based cache.

## Usage (under construction)
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
