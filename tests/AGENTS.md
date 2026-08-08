---
purpose: Route work on runnable Nextflow examples and nf-test fixtures.
applies_to: tests/**
entrypoint: README.md
verification: make test-examples
update_when: Example structure or execution commands change
---

# Runnable fixture map

- Start with [`README.md`](README.md) for provider setup and fixture commands.
- These are runnable Nextflow examples and nf-test fixtures, not the Gradle
  Spock suite under `src/test`.
- Keep each provider scenario self-contained and keep its `README.md`,
  `nextflow.config`, pipeline, and nf-test contract aligned.
- Never commit endpoints or credentials. Keep placeholders in configuration;
  inject live-service values through documented parameters or environment.
- Use the root [`Makefile`](../Makefile) targets to install the plugin and run
  all fixtures or one named fixture.
