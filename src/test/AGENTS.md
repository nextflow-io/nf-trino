---
purpose: Preserve the boundary between deterministic and integration tests.
applies_to: src/test/**
entrypoint: ../../README.md#development
verification: ./gradlew test
update_when: Test taxonomy or Gradle filtering changes
---

# Gradle test map

- [`../../build.gradle`](../../build.gradle) defines the authoritative
  `Integration` exclusion gate.
- Keep ordinary Spock tests deterministic and free of Docker, network, and
  credential requirements.
- Tag provider-specific tests with `Trino`, `Starburst`, or `Athena` as
  applicable.
- Every Docker-, network-, or credential-backed test must also use
  `@Tag("Integration")`; run those explicitly with
  `./gradlew test -Pintegration`.
- Athena live queries require `ATHENA_TEST_S3_OUTPUT_LOCATION` and ambient AWS
  credentials. Trino container tests require Docker.
