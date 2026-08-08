---
purpose: Route plugin implementation changes through their authoritative contracts.
applies_to: src/main/**
entrypoint: groovy/nextflow/plugin/NfTrinoPlugin.groovy
verification: ./gradlew test
update_when: Plugin entry points, extension points, or provider registration change
---

# Plugin implementation map

- [`NfTrinoPlugin.groovy`](groovy/nextflow/plugin/NfTrinoPlugin.groovy) is the
  authoritative driver-registration entry point.
- [`../../build.gradle`](../../build.gradle) owns the plugin class, public
  extension points, dependencies, and packaged Athena driver.
- Trino and Starburst intentionally share `io.trino.jdbc.TrinoDriver`; Athena
  uses `com.amazon.athena.jdbc.AthenaDriver`.
- Keep provider registration, dependencies, the matching guide under
  [`../../docs/`](../../docs/), and tests aligned.
