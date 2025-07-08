# Testing & Continuous Integration

This document describes the testing and CI setup for the nf-trino plugin.

## Test Structure

The plugin uses a comprehensive testing strategy with multiple levels:

### 1. Unit Tests (`make test`)
- Gradle-based unit tests for plugin functionality
- Located in `src/test/groovy/`
- Tests core plugin components and SQL extension points

### 2. Integration Tests (`make test-examples`)
- nf-test based validation of example pipelines
- Tests pipeline syntax and parameter handling
- Located in `tests/*/main.nf.test`
- Validates plugin loading and basic functionality

### 3. Example Pipelines (`tests/*/`)
- 7 comprehensive example pipelines serving as living documentation
- Each includes main.nf, nextflow.config, README.md, and main.nf.test
- Covers Trino, Starburst, and AWS Athena use cases

## CI/CD Workflows

### 1. CI Workflow (`.github/workflows/ci.yml`)
**Trigger**: Push/PR to main branch
**Matrix**: Java 17/21 × Nextflow 24.10/25.04
**Steps**:
- Run unit tests
- Install plugin
- Run integration tests
- Test individual examples

### 2. Test Examples Workflow (`.github/workflows/test-examples.yml`)
**Trigger**: Changes to tests/, src/, or build.gradle
**Matrix**: Nextflow 24.04/24.10/25.04
**Steps**:
- Install nf-test using nf-core/setup-nf-test@v1
- Run all example tests
- Test individual examples

### 3. Validate Plugin Workflow (`.github/workflows/validate.yml`)
**Trigger**: Push/PR to main branch
**Steps**:
- Validate SQL extension loading
- Check pipeline syntax compilation
- Test plugin installation
- Validate documentation examples

### 4. Release Workflow (`.github/workflows/release.yml`)
**Trigger**: Git tags matching `v*`
**Steps**:
- Run full test suite
- Build release artifacts
- Publish to plugin registry
- Create GitHub release

## Local Testing

### Quick Test
```bash
make ci-test  # Runs: clean → test → install → test-examples
```

### Individual Components
```bash
make test                    # Unit tests only
make test-examples          # Integration tests only
make test-example EXAMPLE=test-sql-extension  # Single example
make validate-plugin        # Plugin functionality validation
```

### Manual Testing
```bash
# Test basic plugin loading
cd tests/test-sql-extension
nextflow run main.nf

# Test with specific database (requires credentials)
cd tests/trino-example
nextflow run main.nf --trino_catalog your_catalog
```

## Test Configuration

### nf-test Configuration (`tests/nf-test.config`)
- Sets test directory and work directory
- Allows individual tests to specify their own configs
- Enables running tests from parent directory

### Global Test Config (`tests/nextflow.config`)
- Common configuration for all tests
- Minimal, non-conflicting settings
- Serves as fallback for individual test configs

### Individual Test Configs (`tests/*/nextflow.config`)
- Specific plugin and database configurations
- Can override global settings
- Includes example connection strings and parameters

## Adding New Tests

### Adding a New Example Pipeline:
1. Create directory in `tests/your-example/`
2. Add `main.nf`, `nextflow.config`, `README.md`
3. Create `main.nf.test` following the established pattern:
   ```groovy
   nextflow_pipeline {
       name "Test your-example pipeline"
       script "./main.nf"
       config "./nextflow.config"
       
       test("Your test description") {
           when {
               params {
                   // test parameters
               }
           }
           then {
               // Test that workflow compiles without syntax errors
               assert workflow.success || workflow.failed
               if (workflow.failed) {
                   assert !workflow.errorMessage.contains("Script compilation error")
               }
               if (workflow.success) {
                   // Additional success-dependent assertions
               }
           }
       }
   }
   ```

### Test Patterns:
- **Syntax Validation**: Ensure pipelines compile without syntax errors
- **Parameter Testing**: Validate parameter handling and defaults
- **Error Handling**: Test expected failure modes (e.g., missing credentials)
- **Plugin Loading**: Verify plugin can be loaded (may fail due to credentials)

The testing approach focuses on validating pipeline syntax and structure rather than requiring live database connections, making tests reliable in CI environments.