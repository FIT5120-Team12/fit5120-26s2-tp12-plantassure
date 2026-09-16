# Build Validation

## Completed in the provided execution environment

Main Java sources were compiled successfully using:

```text
javac --release 17
```

with the dependency JARs extracted from the previously built Iteration 1 Spring Boot fat JAR.

This validates Java syntax, imports, Lombok annotation processing and main-source type compatibility after the I2 changes.

## Not executed in the provided execution environment

```text
mvn test
mvn clean package
```

Reason: Maven executable is not installed in this environment.

Before deploying the I2 backend, run locally:

```bash
mvn clean test
mvn clean package
```

Do not deploy the old Iteration 1 JAR with the new source code.
