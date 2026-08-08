# nf-trino Plugin Examples

This directory contains examples demonstrating how to use the nf-trino plugin with Nextflow. Each example is organized as a self-contained pipeline with its own directory, documentation, and tests.

## Prerequisites

1. **Install the plugin**: The plugin should be installed automatically when you run the examples with the `-plugins` flag
2. **Database access**: For real database connections, you'll need appropriate credentials and network access
3. **nf-test**: For running validation tests (optional but recommended)

## Quick Test

To verify the plugin is working correctly:

```bash
# Test basic plugin functionality
cd test-sql-extension
nextflow run main.nf -plugins nf-trino@0.1.0
```

## Example Pipelines

Each example pipeline is contained in its own directory with:
- `main.nf` - The main pipeline script
- `nextflow.config` - Configuration file with database settings
- `README.md` - Detailed documentation and usage instructions
- `main.nf.test` - nf-test validation tests

### 1. Basic Database Connectivity

#### [`test-sql-extension/`](test-sql-extension/)
Tests that the nf-trino plugin SQL extension loads properly without requiring database connections.

```bash
cd test-sql-extension
nextflow run main.nf
```

### 2. Trino Cluster Integration

#### [`trino-example/`](trino-example/)
Demonstrates connecting to a Trino cluster and running basic queries including schema and table discovery.

```bash
cd trino-example
# Configure your Trino connection in nextflow.config first
nextflow run main.nf --trino_catalog hive --trino_schema default
```

### 3. AWS Athena Integration

#### [`athena-example/`](athena-example/)
Basic AWS Athena connection example for querying AWS Glue databases.

```bash
cd athena-example
nextflow run main.nf --organism "Homo sapiens" --limit 20
```

#### [`simple-sra-query/`](simple-sra-query/)
Simple NIH SRA Athena query following established patterns.

```bash
cd simple-sra-query
nextflow run main.nf --organism "Mycobacterium tuberculosis"
```

#### [`simple-athena-test/`](simple-athena-test/)
Connection testing for NIH SRA Athena database using Python scripts.

```bash
cd simple-athena-test
nextflow run main.nf --s3_bucket s3://your-bucket/results/
```

#### [`nih-sra-athena/`](nih-sra-athena/)
Comprehensive NIH SRA Athena pipeline with multiple queries and CSV export.

```bash
cd nih-sra-athena
nextflow run main.nf --organism "Homo sapiens" --assay_type "WGS"
```

### 4. Starburst Integration

#### [`starburst-example/`](starburst-example/)
Demonstrates connecting to Starburst Galaxy or Starburst Enterprise.

```bash
cd starburst-example
nextflow run main.nf --catalog hive --schema genomics --table samples
```

## Testing

Each example includes nf-test validation tests that can be run to verify pipeline syntax and parameter handling:

```bash
# Test a specific pipeline
cd trino-example
nf-test test main.nf.test

# Test all pipelines
nf-test test examples/*/main.nf.test

# Run with specific nf-test configuration
nf-test test -c nf-test.config
```

### Test Profiles

The examples support different testing profiles:

- **ci**: For continuous integration testing (mocks database connections)
- **local**: For local testing with actual database connections

```bash
# Run in CI mode (no real database connections)
nf-test test -profile ci

# Run in local mode (requires database access)
nf-test test -profile local
```

## Configuration

### Trino Configuration

Create a `nextflow.config` file with your Trino connection details:

```groovy
sql {
    db {
        trino {
            url = 'jdbc:trino://your-trino-host:8080/catalog/schema'
            driver = 'trino'
            user = 'your-username'
            // password = 'your-password'  // if needed
        }
    }
}
```

### AWS Athena Configuration

For Athena, configure your connection:

```groovy
sql {
    db {
        awsathena {
            url = 'jdbc:awsathena://AwsRegion=us-east-1;S3OutputLocation=s3://your-bucket/results/'
            driver = 'awsathena'
            // AWS credentials will be picked up automatically from:
            // - Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
            // - AWS credentials file (~/.aws/credentials)
            // - IAM roles (if running on EC2)
        }
    }
}
```

### Starburst Configuration

For Starburst Galaxy (fully managed cloud):

```groovy
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-cluster.galaxy.starburst.io:443/catalog/schema?SSL=true'
            driver = 'starburst'
            user = 'your-username'
            password = 'your-password'
        }
    }
}
```

For Starburst Enterprise (self-managed):

```groovy
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:8080/catalog/schema'
            driver = 'starburst'
            user = 'your-username'
            // Add SSL for secure connections
            // url = 'jdbc:trino://your-starburst-host:443/catalog/schema?SSL=true'
            // password = 'your-password'  // if using LDAP authentication
        }
    }
}
```

## Supported Drivers

The nf-trino plugin registers the following JDBC drivers:

- **trino**: `io.trino.jdbc.TrinoDriver` - For Trino clusters
- **starburst**: `io.trino.jdbc.TrinoDriver` - For Starburst Galaxy and Starburst Enterprise
- **awsathena**: `com.simba.athena.jdbc.Driver` - For AWS Athena

## Troubleshooting

1. **Plugin not found**: Make sure you're using `-plugins nf-trino@0.1.0` in your command
2. **Connection errors**: Verify your database URLs and credentials
3. **AWS permissions**: Ensure you have the necessary IAM permissions for Athena and S3
4. **Network access**: Check that you can reach your database servers

## More Information

- [Trino JDBC Documentation](https://trino.io/docs/current/client/jdbc.html)
- [Starburst JDBC Documentation](https://docs.starburst.io/clients/jdbc.html)
- [AWS Athena JDBC Documentation](https://docs.aws.amazon.com/athena/latest/ug/connect-with-jdbc.html)
- [NIH SRA on AWS](https://www.ncbi.nlm.nih.gov/sra/docs/sra-aws-download/) 