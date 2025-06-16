# Testing Guide

## Test Organization

The nf-trino plugin tests are organized using Spock tags to categorize tests by database type. This allows you to run specific test suites based on the database you're working with.

## Available Test Tags

### `@Tag("Trino")`
Tests related to Trino database functionality:
- Driver registration for Trino
- Trino-specific connection handling
- Tests that apply to both Trino and Starburst (since they use the same driver)

### `@Tag("Starburst")`
Tests related to Starburst Galaxy and Starburst Enterprise functionality:
- Driver registration for Starburst
- Starburst-specific JDBC URL validation
- Connection parameter handling for Galaxy and Enterprise
- Authentication methods (JWT, OAuth, SSL)
- Connection failure scenarios

### `@Tag("Athena")`
Tests related to AWS Athena functionality:
- Driver registration for AWS Athena
- NIH SRA public dataset queries
- SARS-CoV-2 dataset access
- Taxonomy analysis data queries
- AWS credential handling

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Tests by Specific Tag
While Spock tag filtering requires additional configuration, you can run specific test methods using Gradle's test filtering:

```bash
# Run a specific Starburst test
./gradlew test --tests "*should register Starburst driver"

# Run a specific Athena test  
./gradlew test --tests "*should register AWS Athena driver"

# Run a specific Trino test
./gradlew test --tests "*should register Trino driver"

# Run tests matching a pattern
./gradlew test --tests "*Starburst*"
./gradlew test --tests "*Athena*"
```

### Run Tests by Class
```bash
# Run all plugin tests
./gradlew test --tests "nextflow.plugin.NfTrinoPluginTest"

# Run observer tests
./gradlew test --tests "nextflow.plugin.NfTrinoObserverTest"
```

## Test Categories

### Unit Tests
- Driver registration validation
- URL format validation
- Connection parameter handling
- Error handling scenarios

### Integration Tests (Conditional)
Some tests require external resources and are conditionally executed:

- **AWS Athena Tests**: Require AWS credentials (`@Requires` annotation)
  - Environment variables: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
  - AWS profile: `AWS_PROFILE`
  - Credentials file: `~/.aws/credentials`

- **Connection Tests**: Attempt real connections but handle failures gracefully

## Test Results

After running tests, view the detailed HTML report:
```bash
open build/reports/tests/test/index.html
```

The report shows:
- Total tests run, passed, failed, and ignored
- Test execution time
- Detailed failure information
- Test categorization by package and class

## Continuous Integration

For CI environments, you may want to exclude tests that require external credentials:
```bash
# Skip tests that require AWS credentials
./gradlew test --tests "*" --exclude-tests "*NIH*"
```

## Adding New Tests

When adding new tests, use appropriate tags:

```groovy
@Tag("Starburst")
def 'should handle new Starburst feature'() {
    // Test implementation
}

@Tag("Athena") 
@Requires({ /* condition for AWS access */ })
def 'should query new Athena dataset'() {
    // Test implementation
}
```

This organization helps maintain clear separation between different database implementations and makes it easier to run targeted test suites during development. 