All environment specific settings are configured using environment variables (envars) so that these
settings are decoupled from the the artifact packaging.

In development, SpringBoot supports setting envars using the `.env` file. Since these settings often contain
credentials and other sensitive values, this mechanism must **NEVER** be used outside of development use cases.

Here is the list of expected enars, representative values and a description of what they are used for:

    # JDBC URL - standard
    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
    # JDBC URL - OTEL instrumented
    SPRING_DATASOURCE_URL=jdbc:otel:postgresql://localhost:5432/postgres
    # JDBC driver classname - this is usually only required for OTEL instrumentation
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver

    # database credentials - username and password
    # IMPORTANT! never put non-development  credentials in your settings and also never commit them the DVCS
    SPRING_DATASOURCE_USERNAME=dev
    SPRING_DATASOURCE_PASSWORD=4y7sV96vA9wv46VR

    # generate DDL for the JPA models
    SPRING_JPA_GENERATE_DDL=true
    # enable Hibernate DDL updatess (e.g. automatically create tables, etc)
    SPRING_JPA_HIBERNATE_DDL_AUTO=update
    
    # enable OTEL agent instrumentation
    JAVA_TOOL_OPTIONS="-javaagent:/path/to/opentelemetry-javaagent.jar"

    # service name - optional, overrides spring.application.name if set
    OTEL_SERVICE_NAME=Example Service

    # specify the OTEL collector for tracing
    OTEL_TRACES_EXPORTER=logging


    # specify the OTEL collector for metrics
    OTEL_METRICS_EXPORTER=logging

    # specify the OTEL collector for logs
    OTEL_LOGS_EXPORTER=logging