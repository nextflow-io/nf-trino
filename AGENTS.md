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
