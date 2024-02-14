# Task List

A list of tasks still to be completed or under consideration, in no particular order or priority.

## Maven Archetype
The original driver many years ago was the creation of a Maven archetype. In the years
since, the sophistication of the service prototype has grown and it's difficult
to ask maintainers to port the working service to the format and structure required
by the archetype plugin (think text replacement). Maven does not offer the tooling
to do this easily.

## Observability
Integrate OpenTelemetry

## Gradle Archetype
Gradle is both popular and MAY offer a solution to the problems with the Maven based
approach.

## Transactional Service Tier
Currently, the protocol and service tiers are flattened into one. 

## AuthN
Integrate Spring Security. This has been deferred as I'm experimenting with containerization
and sidecar patterns where AuthN is moved to a sidecar app.

## Configuration
The current version used command line options to configure environmental aspects
(e.g. database, etc). This should be changed to envars.

