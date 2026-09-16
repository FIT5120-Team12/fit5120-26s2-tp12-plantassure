# Epic 1 Backend Change Log

This revision extends the previously completed Iteration 2 backend (Epic 2 + Epic 3) and does not delete any existing Java source file.

## Existing Java files modified

```text
src/main/java/com/plantky/common/enums/ErrorCode.java
src/main/java/com/plantky/common/handler/GlobalExceptionHandler.java
```

Changes:

- added `INVALID_IMAGE`;
- added `IDENTIFICATION_FAILED`;
- added `IDENTIFICATION_SERVICE_UNAVAILABLE`;
- added missing multipart image handling;
- added over-size multipart handling.

## New main Java files

```text
client/identification/PlantIdentificationClient.java
client/identification/HttpPlantIdentificationClient.java
client/identification/dto/AiIdentificationCandidate.java

common/enums/IdentificationStatus.java

config/PlantIdentificationProperties.java
config/PlantIdentificationClientConfig.java

controller/PlantIdentificationController.java

domain/vo/identification/IdentificationMatchVO.java
domain/vo/identification/PlantIdentificationResponse.java

service/PlantIdentificationService.java
service/component/ImageValidationService.java
service/component/SpeciesMatchingService.java
service/impl/PlantIdentificationServiceImpl.java
```

Package-info files were also added for the new packages.

## Existing configuration modified

```text
src/main/resources/application.yml
```

Added:

- Spring multipart 10 MB limit;
- AI provider enable flag;
- provider URL/endpoint/key;
- Top 3 candidate limit;
- configurable confidence threshold;
- connect/read timeouts.

## New tests

```text
PlantIdentificationControllerTest.java
ImageValidationServiceTest.java
PlantIdentificationServiceImplTest.java
```

## Documentation added

```text
EPIC1_AI_IDENTIFICATION.md
EPIC1_AC_TRACEABILITY.md
EPIC1_APIFOX_TESTS.md
EPIC1_CHANGELOG.md
```

## Database

No new database table is required for the Epic 1 MVP.

The existing stable `species_data.id` is reused as `plantId` after scientific-name matching.
A future verified synonym table is intentionally deferred until the team has an authoritative synonym source.
