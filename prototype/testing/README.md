# Automated Test Suite

This module is executed post-build as part of a CI/CD pipeline and used 
to test a deployed release candidate of the service. The tests ensure 
the expected functioning of the service as well as non-functional 
aspects such as performance and security. A release candidate
should be tested in a fully integrated environment.

Extensive functional and non-functional testing is required here as 
the tests in this module will help to determine if a release
candidate should be considered for promotion toward to next stage
on a path to production.

## Smoke Testing
Test the correct deployment of the service to its runtime environment.
This ensures the deployment was successful, the configuration
is correct, and a small subset of functional tests can pass. These
are also known as sanity tests and should be run prior to any of the 
other tests as test execution tends to be time-consuming. If 
one or more of these tests fail, then the full suite will 
likewise fail and is pointless to execute. Any problems that 
arise here should be dealt with before attempting to execute 
the full test suite.

## Acceptance Testing
Tests the behavior of the API in terms of its service contract. These should be
performed against a service deployment that is integrated with its dependencies,
such as databases, external services, etc.

RestAssured is recommended for this purpose as it provides a simple but elegant,
Behavior Driven Testing (BDD) styled test client.

## Regression Testing
Testing that ensures that the release candidate introduces no 
unintended breaks. Regression testing addresses a common issue 
that developers face — the emergence of old bugs with the 
introduction of new changes. These should be written similar to
acceptance tests.

Each bug (or regression) should be accompanied by a test that 
ensures the correctness of the fix. These tests can be collected
and maintained here.

## Fuzz Testing
A technique that involves providing invalid, unexpected, or 
random data as inputs to a computer program. The program is 
then monitored for exceptions such as crashes, failing built-in 
code assertions, or potential memory leaks. Typically, fuzzers 
are used to test programs that take structured inputs.

SEE: https://github.com/EMResearch/EvoMaster/tree/master?tab=readme-ov-file

## Performance Testing
Testing to measure stability and responsiveness in the face of 
increased stress and load.

JMeter is recommended for performance testing.

## Security Testing
Tests that screen the software for any vulnerabilities to reveal weaknesses
and any potential exploit in a system.

Static analysis testing may be here or in the build itself (or both)
depending on the time they take to complete and how frequently they
tend to fail.

## Chaos Testing
An approach to testing a system's integrity by proactively 
simulating and identifying failures in a given environment 
before they lead to unplanned downtime or a negative user 
experience.

