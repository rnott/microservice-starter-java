# Task List

A list of tasks still to be completed or under consideration, in no particular order or priority.

## Maven Archetype
The original driver many years ago was the creation of a Maven archetype. In the years
since, the sophistication of the service prototype has grown and it's difficult
to ask maintainers to port the working service to the format and structure required
by the archetype plugin (think text replacement). Maven does not offer the tooling
to do this easily.

## Gradle Archetype
Gradle is both popular and MAY offer a solution to the problems with the Maven based
approach.

## Transactional Service Tier
Currently, the protocol and service tiers are flattened into one. 

## Idempotency
Promote this style (maybe remove the POST?)

## AuthN
Integrate Spring Security. This has been deferred as I'm experimenting with containerization
and sidecar patterns where AuthN is moved to a sidecar app.

## Startup Speed
For ephemeral, auto-scaling runtimes startup spped is a critical concern.

## HATEOAS
Use filters to support hypermedia links.

## Cache Control
Use filters and features to implement cache control.

