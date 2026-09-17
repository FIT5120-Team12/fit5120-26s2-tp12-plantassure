# Build Validation

## Validation performed in the artifact environment

The environment does not provide the `mvn` executable, so the complete Maven/JUnit lifecycle could not be executed here.

Main source validation was performed using:

```text
javac --release 17
```

with the compile/runtime dependency JARs extracted from the previously built Spring Boot fat JAR.

Result after Epic 1 integration:

```text
105 main Java source files compiled successfully
```

This verifies Java syntax/type compatibility for the current main source tree against the existing Spring Boot/MyBatis/Lombok dependencies.

## Tests added for Epic 1

```text
PlantIdentificationControllerTest
ImageValidationServiceTest
PlantIdentificationServiceImplTest
```

They cover the HTTP response shape, missing multipart image handling, image signature validation, Top 3 behaviour, no-match state, and unmapped-AI-candidate behaviour.

Because Maven/test-scope dependencies are unavailable in this execution environment, these JUnit tests must be run locally.

## Required local validation before deployment

```bash
mvn clean test
mvn clean package
```

Then start the backend and use the Apifox cases in:

```text
docs/EPIC1_APIFOX_TESTS.md
```
