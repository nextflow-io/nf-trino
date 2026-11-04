---
purpose: Route coding agents to the project's canonical maintenance guidance.
applies_to: Entire repository
entrypoint: README.md
verification: ./gradlew test
update_when: Contributor or release workflows change
---

# Agent map

- Start with [`README.md`](README.md) for the public plugin contract and
  developer commands.
- Use [`src/main/AGENTS.md`](src/main/AGENTS.md) for plugin registration,
  extension points, and dependency contracts.
- Use [`src/test/AGENTS.md`](src/test/AGENTS.md) for Gradle test boundaries.
- Use [`tests/AGENTS.md`](tests/AGENTS.md) for runnable Nextflow and nf-test
  fixtures.
- Use [`docs/AGENTS.md`](docs/AGENTS.md) for provider-facing documentation.
- Maintain releases in [`CHANGELOG.md`](CHANGELOG.md); follow its Keep a
  Changelog and Semantic Versioning guidance.

## Change ownership

Fix behavior in the lowest module that owns the affected interface:

- `nf-trino` owns JDBC driver registration and packaging, provider-specific
  compatibility, documentation, and runnable examples.
- `nf-sqldb` owns generic SQL configuration and query behavior, including
  parameter binding and datasource property forwarding.
- Nextflow core owns plugin loading, lifecycle, and DSL behavior.

Keep a workaround in this repository only when it is provider-specific or an
upstream release is not yet available. Link the upstream issue or change and
remove the workaround when the dependency is updated.
