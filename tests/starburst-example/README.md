# Starburst Example Pipeline

This example demonstrates how to use the nf-trino plugin to connect to Starburst Galaxy or Starburst Enterprise.

## Description

This pipeline shows how to:
- Connect to Starburst Galaxy (fully managed cloud) or Starburst Enterprise
- Test database connectivity
- Query available catalogs
- Execute data queries and aggregations
- Use various Starburst authentication methods

## Requirements

- Starburst Galaxy account or Starburst Enterprise deployment
- Database credentials (username/password, JWT token, or OAuth)
- Network access to your Starburst cluster

## Configuration

Edit the `nextflow.config` file to configure your Starburst connection. Choose the appropriate configuration based on your deployment:

### Starburst Galaxy
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

### Starburst Enterprise
```groovy
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:8080/catalog/schema'
            driver = 'starburst'
            user = 'your-username'
            // password = 'your-password'  // if using LDAP
        }
    }
}
```

## Usage

```bash
# Run with default parameters
nextflow run main.nf

# Run with custom parameters
nextflow run main.nf \
    --catalog 'hive' \
    --schema 'genomics' \
    --table 'samples' \
    --limit 20

# Run with specific database configuration
nextflow run main.nf \
    --db_name 'starburst_galaxy' \
    --catalog 'your_catalog'
```

## Parameters

- `db_name`: Database configuration name (default: 'starburst')
- `catalog`: Starburst catalog name (default: 'your_catalog')
- `schema`: Schema name (default: 'your_schema')
- `table`: Table name (default: 'your_table')
- `limit`: Maximum number of results (default: 10)

## Testing

Run the nf-test suite:

```bash
nf-test test main.nf.test
```

## Expected Output

The pipeline will execute several queries and display:
1. Connection test results
2. Available catalogs
3. Sample data rows
4. Aggregation summary

```
Connection test: Starburst connection successful!
Available catalog: hive
Available catalog: postgresql
Data row: [col1: value1, col2: value2, ...]
Summary: 1000 rows found via Summary from Starburst
```