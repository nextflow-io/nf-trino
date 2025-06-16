# Starburst integration

## Pre-requisites

1. A running Starburst cluster (Galaxy or Enterprise)
2. Network access to the Starburst coordinator
3. Appropriate authentication credentials (depending on your Starburst security setup)
4. Access to query tables in the `system.jdbc` schema

## Usage

The following examples demonstrate how to connect to Starburst and execute queries using the nf-trino plugin.

### Basic Configuration for Starburst Galaxy

Starburst Galaxy is the fully managed cloud service. Configure your connection as follows:

```nextflow config
plugins {
    id 'nf-trino'
}

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

### Basic Configuration for Starburst Enterprise

For self-managed Starburst Enterprise deployments:

```nextflow config
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:8080/catalog/schema'
            driver = 'starburst'
            user = 'your-username'
        }
    }
}
```

### Secure Configuration (SSL/TLS)

For production environments with SSL enabled:

```nextflow config
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:443/catalog/schema?SSL=true'
            driver = 'starburst'
            user = 'your-username'
            password = 'your-password'  // For LDAP authentication
        }
    }
}
```

### JWT Authentication

For clusters using JWT authentication:

```nextflow config
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:8080/catalog/schema'
            driver = 'starburst'
            user = 'your-username'
            // JWT token should be passed as a connection property
            properties = [
                accessToken: 'your-jwt-token'
            ]
        }
    }
}
```

### OAuth 2.0 Authentication

For external authentication via OAuth 2.0:

```nextflow config
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:8080/catalog/schema?externalAuthentication=true'
            driver = 'starburst'
            user = 'your-username'
        }
    }
}
```

### Pipeline Examples

#### Basic Query

Execute a simple query against your Starburst cluster:

```nextflow
include { fromQuery } from 'plugin/nf-sqldb'

def sqlQuery = """
    SELECT customer_id, order_date, total_amount
    FROM orders
    WHERE order_date >= DATE '2023-01-01'
    LIMIT 100
    """

Channel.fromQuery(sqlQuery, db: 'starburst').view()
```

#### Parameterized Query

Use parameters in your queries for dynamic data selection:

```nextflow
include { fromQuery } from 'plugin/nf-sqldb'

params.start_date = '2023-01-01'
params.limit = 100

def sqlQuery = """
    SELECT customer_id, order_date, total_amount
    FROM orders
    WHERE order_date >= DATE '${params.start_date}'
    LIMIT ${params.limit}
    """

Channel.fromQuery(sqlQuery, db: 'starburst').view()
```

#### Data Lake Query

Query data from your data lake through Starburst:

```nextflow
include { fromQuery } from 'plugin/nf-sqldb'

def sqlQuery = """
    SELECT 
        sample_id,
        experiment_type,
        file_path
    FROM iceberg_catalog.genomics.samples
    WHERE experiment_type = 'RNA-seq'
    AND processing_status = 'completed'
    """

Channel.fromQuery(sqlQuery, db: 'starburst')
    .map { row -> 
        tuple(row.sample_id, file(row.file_path))
    }
    .set { sample_files }
```

#### Cross-Catalog Federation

Leverage Starburst's federation capabilities to query across multiple data sources:

```nextflow
include { fromQuery } from 'plugin/nf-sqldb'

def federatedQuery = """
    SELECT 
        s.sample_id,
        s.patient_id,
        m.gene_expression_level
    FROM postgres_catalog.clinical.samples s
    JOIN iceberg_catalog.omics.gene_expression m
        ON s.sample_id = m.sample_id
    WHERE s.study_id = 'STUDY_001'
    """

Channel.fromQuery(federatedQuery, db: 'starburst').view()
```

## Advanced Configuration

### Connection Properties

You can specify additional connection properties for fine-tuning:

```nextflow config
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:8080/hive/default'
            driver = 'starburst'
            user = 'analytics-user'
            properties = [
                SSL: 'true',
                SSLVerification: 'FULL',
                source: 'nextflow-analytics',
                clientTags: 'batch,genomics',
                sessionProperties: 'query_max_memory=10GB;query_max_run_time=1h',
                roles: 'hive:analyst;system:monitor',
                timezone: 'UTC'
            ]
        }
    }
}
```

### Spooling Protocol

For high-throughput queries, Starburst automatically uses the spooling protocol when configured:

```nextflow config
sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:8080/catalog/schema'
            driver = 'starburst'
            user = 'your-username'
            properties = [
                encoding: 'json+zstd'  // Use Zstandard compression for better performance
            ]
        }
    }
}
```

## Troubleshooting

### Common Issues

1. **Connection Timeout**: Ensure network connectivity to the Starburst coordinator
2. **Authentication Failed**: Verify username/password or JWT token validity
3. **Permission Denied**: Check that the user has access to the `system.jdbc` schema
4. **SSL Certificate Issues**: Use `SSLVerification=NONE` for testing (not recommended for production)

### Testing Connection

You can test your connection with a simple query:

```nextflow
def testQuery = "SELECT 'Connection successful' as status"
Channel.fromQuery(testQuery, db: 'starburst').view()
```

### Galaxy-Specific Troubleshooting

For Starburst Galaxy:
- Ensure your cluster is running and not suspended
- Verify your Galaxy credentials and cluster URL
- Check that SSL is enabled in your connection string

### Enterprise-Specific Troubleshooting

For Starburst Enterprise:
- Verify the coordinator host and port
- Check firewall rules and network connectivity
- Ensure the cluster is properly configured and running

## Performance Considerations

- Use `LIMIT` clauses for large result sets
- Consider using Starburst's built-in spooling protocol for high-throughput queries
- Enable compression for better network performance: `disableCompression=false`
- Use appropriate session properties for memory and time limits
- Leverage Starburst's query acceleration features like Warp Speed when available

## Security Best Practices

- Always use SSL/TLS for production connections
- Store credentials securely using environment variables or secret management
- Use least-privilege access principles
- Enable audit logging for compliance requirements
- Consider using OAuth 2.0 or JWT for enhanced security

For more detailed information, refer to the [official Starburst JDBC documentation](https://docs.starburst.io/clients/jdbc.html). 