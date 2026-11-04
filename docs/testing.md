---
purpose: Define deterministic, container, and live-service testing for nf-trino
applies_to: Plugin, JDBC driver, and example changes
entrypoint: ./gradlew test
verification: Run the command for each affected test tier
update_when: Test commands, credentials, drivers, or CI coverage change
---
# Testing guide

Use the lowest test tier that exercises the changed behavior. CI intentionally runs only the deterministic tier; Docker and credential-backed services remain explicit opt-in checks.

## Test tiers

| Tier | Command | Use when |
| --- | --- | --- |
| Deterministic | `./gradlew test` | Every change and every pull request |
| Trino container | `./gradlew test -Pintegration --tests 'nextflow.plugin.TrinoContainerTest'` | Trino JDBC, SQL behavior, or shared Trino/Starburst driver changes |
| Live Athena | `./gradlew test -Pintegration --tests 'nextflow.plugin.NfTrinoPluginTest'` | Athena driver, credentials, URL, or query changes |
| Live Starburst | Run `tests/starburst-example/main.nf` against a temporary cluster | Starburst endpoint, TLS, or authentication changes |

Testcontainers starts a real Trino server in Docker. It is not a mock, and it does not reproduce Athena or Starburst-specific service and authentication behavior.

## Deterministic CI

```bash
./gradlew test
```

The default Gradle test task excludes tests tagged `Integration`. The GitHub Actions workflow runs this command with Java 17 and no Docker, AWS credentials, or database secrets.

Use focused tests while developing:

```bash
./gradlew test --tests '*should register Trino driver'
./gradlew test --tests '*should register Starburst driver'
./gradlew test --tests '*should register AWS Athena driver'
```

## Trino with Testcontainers

Requirements:

- a running Docker-compatible container runtime
- enough resources to start `trinodb/trino:451`

Run only the container suite so enabling integration tests does not also select credential-backed Athena tests:

```bash
./gradlew test -Pintegration --tests 'nextflow.plugin.TrinoContainerTest'
```

This suite verifies a real JDBC connection, DDL and DML, Trino data types, catalogs and schemas, and driver-registry connectivity. Run it after changing the Trino JDBC version or code shared by Trino and Starburst.

## Live Athena

Requirements:

- AWS credentials available through the default AWS credential chain
- permission to query Athena in `us-east-1`
- permission to write query results to a dedicated S3 prefix

```bash
export ATHENA_TEST_S3_OUTPUT_LOCATION='s3://YOUR-BUCKET/nf-trino-tests/'
aws sts get-caller-identity
./gradlew test -Pintegration --tests 'nextflow.plugin.NfTrinoPluginTest'
```

The Athena integration cases query the public NIH SRA catalogs and write results to `ATHENA_TEST_S3_OUTPUT_LOCATION`. Use a disposable prefix and delete its objects after the run.

Run this tier after changing the Athena JDBC artifact or version, JDBC properties, credential handling, region, catalog, database, workgroup, or output-location behavior. Do not put long-lived AWS keys in repository settings or workflow files.

## Live Starburst

There is no Starburst Testcontainers module in this repository. The Trino container suite covers the shared `io.trino.jdbc.TrinoDriver`, but only a real Starburst deployment can validate its endpoint, TLS, and authentication behavior.

1. Provision a temporary Starburst Galaxy or Enterprise cluster and a least-privilege test identity.
2. Put its JDBC configuration in an untracked Nextflow config outside the repository.
3. Install the plugin and run the example with that config:

```bash
make install
nextflow run tests/starburst-example/main.nf \
  -c /absolute/path/to/starburst-test.config \
  --db_name starburst \
  --catalog YOUR_CATALOG \
  --schema YOUR_SCHEMA \
  --table YOUR_TABLE \
  --limit 1
```

A successful run must connect, list catalogs, and return the expected test row. Destroy the temporary cluster or revoke the test identity after the run.

Run this tier after changing Starburst URL construction, TLS, username/password, JWT, OAuth, or vendor-specific connection properties. A Trino container run alone is insufficient for those changes.

## nf-test examples

```bash
make test-examples
make test-example EXAMPLE=test-sql-extension
```

These fixtures validate example parsing and parameter wiring. Athena and Starburst fixtures are not substitutes for the live-service checks above: an expected connection failure can still satisfy some fixture assertions.

## Adding tests

- Keep deterministic tests untagged so CI runs them.
- Tag Docker and credential-backed tests with `@Tag("Integration")`.
- Add database tags such as `Trino`, `Starburst`, or `Athena` for discoverability.
- Never catch or tolerate an unexpected failure merely to make CI pass.
