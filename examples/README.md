# nf-trino Plugin Examples

This directory contains examples demonstrating how to use the nf-trino plugin with Nextflow.

## Prerequisites

1. **Install the plugin**: The plugin should be installed automatically when you run the examples with the `-plugins` flag
2. **Database access**: For real database connections, you'll need appropriate credentials and network access

## Quick Test

To verify the plugin is working correctly:

```bash
# Test basic plugin functionality
nextflow run test-sql-extension.nf -plugins nf-trino@0.1.0
```

## Examples

### 1. Trino Connection Example

**File**: `trino-example.nf`

Demonstrates connecting to a Trino cluster and running basic queries.

```bash
# Configure your Trino connection in trino-config.config first
nextflow run trino-example.nf -c trino-config.config -plugins nf-trino@0.1.0
```

### 2. AWS Athena Examples

**Files**: 
- `athena-example.nf` - Basic Athena connection
- `nih-sra-athena.nf` - NIH SRA public dataset queries
- `simple-athena-test.nf` - Connection testing
- `simple-sra-query.nf` - Simple SRA queries

For AWS Athena examples, you need:
- AWS credentials configured (via AWS CLI, environment variables, or IAM roles)
- An S3 bucket for query results
- Appropriate permissions for Athena and S3

```bash
# Basic Athena example
nextflow run athena-example.nf -plugins nf-trino@0.1.0 \
  --aws_glue_db your-database \
  --aws_glue_db_table your-table

# NIH SRA public data example (requires AWS credentials)
nextflow run nih-sra-athena.nf -plugins nf-trino@0.1.0 \
  --s3_bucket s3://your-bucket/results/
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