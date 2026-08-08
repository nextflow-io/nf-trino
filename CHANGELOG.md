---
purpose: Track user-visible changes and release history.
applies_to: Plugin releases
entrypoint: Unreleased
verification: Review Unreleased entries before release
update_when: User-visible behavior changes
---

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

Add user-visible changes under **Unreleased**. For each release, move those
entries to `## [X.Y.Z] - YYYY-MM-DD` and choose the version according to
Semantic Versioning.

## [Unreleased]

### Added

- Initial Nextflow plugin support for querying Trino, Starburst, and Amazon Athena.
- Unit and integration test coverage for supported database providers.

### Changed

- Continuous integration runs deterministic unit tests by default.
- Upgraded the Amazon Athena JDBC driver from Simba 2.2.1 to AWS 3.8.0.

[Unreleased]: https://github.com/nextflow-io/nf-trino/commits/main
