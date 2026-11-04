# Trino Example Pipeline

This example demonstrates how to use the nf-trino plugin to query a Trino cluster directly.

## Description

This pipeline shows how to:
- Connect to a Trino cluster using the nf-trino plugin
- Test database connectivity
- Query information schema for available schemas and tables
- Use different Trino catalogs (Hive, PostgreSQL, etc.)

## Requirements

- Access to a Trino cluster
- Network connectivity to the Trino coordinator
- Appropriate authentication credentials (if required)

## Configuration

Edit the `nextflow.config` file to configure your Trino connection:

```groovy
sql {
    db {
        trino {
            url = 'jdbc:trino://trino-coordinator.example.com:8080/hive/default'
            driver = 'trino'
            user = 'nextflow'
            // password = 'your-password'  // if using LDAP authentication
        }
    }
}
```

### SSL Configuration
For SSL connections, add connection properties:

```groovy
sql {
    db {
        trino {
            url = 'jdbc:trino://trino-coordinator.example.com:443/hive/default'
            driver = 'trino'
            user = 'nextflow'
            properties = [
                SSL: 'true',
                SSLVerification: 'FULL'
            ]
        }
    }
}
```

## Usage

```bash
# Run with default parameters
nextflow run main.nf

# Run with custom catalog and schema
nextflow run main.nf \
    --trino_catalog 'postgresql' \
    --trino_schema 'public' \
    --limit 20

# Run with Hive catalog
nextflow run main.nf \
    --trino_catalog 'hive' \
    --trino_schema 'genomics'
```

## Parameters

- `trino_catalog`: Trino catalog name (default: 'hive')
- `trino_schema`: Schema name (default: 'default')
- `limit`: Maximum number of results (default: 10)

## Testing

Run the nf-test suite:

```bash
nf-test test main.nf.test
```

## Expected Output

The pipeline will execute three queries and display:
1. Connection test with timestamp
2. Available schemas in the specified catalog
3. Available tables in the specified schema

```
✅ Connection Test: Trino connection successful at 2024-01-15 10:30:45.123
📋 Schema: hive.default (owner: admin)
📋 Schema: hive.genomics (owner: scientist)
📊 Table: hive.default.samples (type: BASE TABLE)
📊 Table: hive.default.results (type: BASE TABLE)
```

## Common Catalogs

- `hive`: Apache Hive metastore
- `postgresql`: PostgreSQL database
- `mysql`: MySQL database
- `memory`: In-memory connector for testing
- `tpch`: TPC-H benchmark data