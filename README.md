# nf-trino plugin

[![CI](https://github.com/nextflow-io/nf-trino/actions/workflows/ci.yml/badge.svg)](https://github.com/nextflow-io/nf-trino/actions/workflows/ci.yml)
[![Test Examples](https://github.com/nextflow-io/nf-trino/actions/workflows/test-examples.yml/badge.svg)](https://github.com/nextflow-io/nf-trino/actions/workflows/test-examples.yml)
[![Validate Plugin](https://github.com/nextflow-io/nf-trino/actions/workflows/validate.yml/badge.svg)](https://github.com/nextflow-io/nf-trino/actions/workflows/validate.yml)

This plugin provides support for Trino, Starburst, and AWS Athena SQL databases in Nextflow workflows.

## Features

- **Trino Integration**: Query Trino databases directly from Nextflow workflows using the official Trino JDBC driver
- **Starburst Integration**: Query Starburst Galaxy and Starburst Enterprise platforms using the same Trino JDBC driver
- **AWS Athena Integration**: Query AWS Athena databases directly from Nextflow workflows
- **SQL Channel Extension**: Leverage the `nf-sqldb` channel extensions for seamless data integration

## Building

To build the plugin:
```bash
make assemble
```

## Usage

### Trino Connection

Connect to a Trino cluster using JDBC URL format:

```groovy
// Connect to Trino
def sql = sql {
    url = "jdbc:trino://example.net:8080/catalog/schema"
    driver = "trino"
    user = "your-username"
    // Optional parameters
    // password = "your-password"  // for LDAP authentication
    // SSL = "true"               // for secure connections
    // accessToken = "jwt-token"  // for JWT authentication
}

// Query data
channel.fromQuery("SELECT * FROM your_table", sql: sql)
```

See the [Trino documentation](https://trino.io/docs/current/client/jdbc.html) for detailed connection parameters and authentication options.

### Starburst Connection

Connect to Starburst Galaxy or Starburst Enterprise:

```groovy
// Connect to Starburst Galaxy (fully managed cloud)
def sql = sql {
    url = "jdbc:trino://your-cluster.galaxy.starburst.io:443/catalog/schema?SSL=true"
    driver = "starburst"
    user = "your-username"
    password = "your-password"
}

// Connect to Starburst Enterprise (self-managed)
def sql = sql {
    url = "jdbc:trino://your-starburst-host:8080/catalog/schema"
    driver = "starburst"
    user = "your-username"
}
```

See the [Starburst documentation](docs/starburst.md) for detailed usage instructions and authentication options.

### AWS Athena Connection

See the [AWS Athena documentation](docs/aws-athena.md) for detailed usage instructions.

## Testing

### Running Tests

The plugin includes comprehensive test suites:

```bash
# Run unit tests
make test

# Run integration tests with nf-test
make test-examples

# Run all tests (unit + integration)
make ci-test

# Test a specific example
make test-example EXAMPLE=test-sql-extension

# Clean up test artifacts
make clean-tests
```

### Example Pipelines

The plugin includes 7 example pipelines that serve as living documentation:

- [`test-sql-extension`](tests/test-sql-extension/) - Tests basic SQL extension loading
- [`trino-example`](tests/trino-example/) - Trino cluster integration example
- [`athena-example`](tests/athena-example/) - Basic AWS Athena integration
- [`starburst-example`](tests/starburst-example/) - Starburst Galaxy/Enterprise integration
- [`simple-athena-test`](tests/simple-athena-test/) - NIH SRA Athena connection test
- [`simple-sra-query`](tests/simple-sra-query/) - Basic NIH SRA query example
- [`nih-sra-athena`](tests/nih-sra-athena/) - Comprehensive NIH SRA pipeline

Each example includes:
- `main.nf` - The pipeline script
- `nextflow.config` - Database configuration
- `main.nf.test` - nf-test validation tests
- `README.md` - Documentation and usage instructions

### Continuous Integration

The project uses GitHub Actions for CI/CD with three workflows:

1. **CI** (`ci.yml`) - Runs unit tests and integration tests across Java 17/21 and Nextflow 24.10/25.04
2. **Test Examples** (`test-examples.yml`) - Tests example pipelines across multiple Nextflow versions
3. **Validate Plugin** (`validate.yml`) - Validates plugin functionality and documentation

### Manual Testing

The plugin can be tested manually without database connections:

1. Build and install the plugin: `make install`
2. Test basic functionality: `cd tests/test-sql-extension && nextflow run main.nf`
3. Test with your own database: `cd tests/trino-example && nextflow run main.nf --trino_catalog your_catalog`

## Publishing

Plugins can be published to a central plugin registry to make them accessible to the Nextflow community. 


Follow these steps to publish the plugin to the Nextflow Plugin Registry:

1. Create a file named `$HOME/.gradle/gradle.properties`, where $HOME is your home directory. Add the following properties:

    * `pluginRegistry.accessToken`: Your Nextflow Plugin Registry access token. 

2. Use the following command to package and create a release for your plugin on GitHub: `make release`.


> [!NOTE]
> The Nextflow Pluging registry is currently avaialable as private beta technology. Contact info@nextflow.io to learn how to get access to it.
> 