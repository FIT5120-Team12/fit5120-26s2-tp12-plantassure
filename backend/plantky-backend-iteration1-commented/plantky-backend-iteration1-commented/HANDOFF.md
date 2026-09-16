# Iteration 2 Backend Handoff — Epic 1 + Epic 2 + Epic 3

Start here:

1. `README.md`
2. `docs/EPIC1_AI_IDENTIFICATION.md`
3. `docs/EPIC1_AC_TRACEABILITY.md`
4. `docs/ITERATION2_BACKEND_IMPLEMENTATION.md`
5. `docs/ITERATION2_AC_TRACEABILITY.md`
6. `src/main/resources/db/species_data_i2_backend.sql`

Before real Epic 1 integration, obtain from the AI teammate:

1. real AI base URL + endpoint;
2. real multipart request field/headers;
3. real response JSON example;
4. validated no-confident-match threshold/rule.

Then configure the environment variables documented in `README.md` and narrow only the provider adapter if its response differs.

Run locally:

```bash
mvn clean test
mvn clean package
```

Do not deploy an old Iteration 1/earlier Iteration 2 JAR with this source tree.
