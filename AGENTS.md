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
- Route plugin registration and dependency changes through
  [`build.gradle`](build.gradle) and
  [`NfTrinoPlugin.groovy`](src/main/groovy/nextflow/plugin/NfTrinoPlugin.groovy).
- Use [`src/test/AGENTS.md`](src/test/AGENTS.md) for Gradle test boundaries.
- Use [`tests/AGENTS.md`](tests/AGENTS.md) for runnable Nextflow and nf-test
  fixtures.
- Keep provider-facing changes aligned with the matching guide under
  [`docs/`](docs/).
- Maintain releases in [`CHANGELOG.md`](CHANGELOG.md); follow its Keep a
  Changelog and Semantic Versioning guidance.
