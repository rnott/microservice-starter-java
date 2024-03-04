# Observability - Telemetry Instrumentation

OpenTelemetry is an observability framework and toolkit designed to create and manage telemetry data 
such as traces, metrics, and logs. Crucially, OpenTelemetry is vendor- and tool-agnostic, meaning 
that it can be used with a broad variety of observability backends, including open source tools like 
Jaeger and Prometheus, as well as commercial offerings.

OpenTelemetry is not an observability backend like Jaeger, Prometheus, or other commercial vendors. 
OpenTelemetry is focused on the generation, collection, management, and export of telemetry. A major 
goal of OpenTelemetry is that you can easily instrument your applications or systems, no matter their 
language, infrastructure, or runtime environment. Crucially, the storage and visualization of telemetry 
is intentionally left to other tools

## Spring Boot Starter
Instrumentation is enabled through the use of build time dependencies and configuration.
The primary advantage is Spring integration, while the primary disadvantage is the lack
of out of the box integrations with Undertow, JAX-RS, etc. This needs work to support
various integrations.

Future work will involve ways to instrument these frameworks in addition to what is offered
by the starter.

## Java Agent
All in one, agent based instrumentation for the application. The main advantage of this
approach is support for a wide variety of libraries and frameworks, while the main 
disadvantage is startup time as the agent generates byte code. This option is used for now.

Download the agent and enable using the following envars:

    JAVA_TOOL_OPTIONS="-javaagent:path/to/opentelemetry-javaagent.jar"

## Configuration

### JDBC
To configure instrumentation of database operations (queries, etc) add the following envars:

    SPRING_DATASOURCE_URL=jdbc:otel:postgresql://localhost:5432/postgres
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver

### Logging
To inject MDC information (trace and span ids) into log entries, add the following application configuration:

    logging.pattern.level: trace_id=%mdc{trace_id} span_id=%mdc{span_id} trace_flags=%mdc{trace_flags} %5p

This will allow queries to seach for log entries by trance, span, etc.

## Exporters
OpenTelemetry supports exporting telemetry data to various collectors that support the protocol.

### Log Appender
Logback and Log4j logging frameworks are supported. 

    OTEL_TRACES_EXPORTER=logging
    OTEL_METRICS_EXPORTER=logging
    OTEL_LOGS_EXPORTER=logging

## Resources
- [Java OpenTelemetry Examples](https://github.com/open-telemetry/opentelemetry-java-examples/tree/main)
- [How to instrument Spring Boot with OpenTelemetry](https://opentelemetry.io/docs/languages/java/automatic/spring-boot/)
- [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#disabling-opentelemetrysdk)
- [OpenTelemetry instrumentation libraries](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md#libraries--frameworks)
- [A curated list of OpenTelemetry resource](https://github.com/magsther/awesome-opentelemetry?tab=readme-ov-file)
- 