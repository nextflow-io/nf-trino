---
purpose: Route provider documentation changes to matching public contracts.
applies_to: docs/**
entrypoint: ../README.md
verification: Check links and matching runnable fixtures
update_when: Provider behavior, configuration, or examples change
---

# Provider documentation map

- Each provider guide owns its connection and configuration guidance.
- Keep public behavior summarized in [`../README.md`](../README.md) and runnable
  contracts aligned under [`../tests/`](../tests/).
- When provider behavior changes, update the matching guide, fixture, and
  registration or dependency contract routed by
  [`../src/main/AGENTS.md`](../src/main/AGENTS.md).
- Use placeholders only; never commit endpoints, credentials, or writable S3
  locations.
