# Trino integration

## Pre-requisites

1. A running Trino cluster
2. Network access to the Trino coordinator
3. Appropriate authentication credentials (depending on your Trino security setup)
4. Access to query tables in the `system.jdbc` schema

## Usage

The following examples demonstrate how to connect to Trino and execute queries using the nf-trino plugin.

### Basic Configuration

Adjust the following configuration to match your setup:

```nextflow config
plugins {
    id 'nf-trino'
}

sql {
    db {
        trino {
            url = 'jdbc:trino://trino-coordinator.example.com:8080/catalog/schema'
            driver = 'trino'
            user = 'your-username'
        }
    }
}
```

### Secure Configuration (SSL)

For production environments with SSL enabled:

```nextflow config
sql {
    db {
        trino {
            url = 'jdbc:trino://trino-coordinator.example.com:443/catalog/schema?SSL=true'
            driver = 'trino'
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
        trino {
            url = 'jdbc:trino://trino-coordinator.example.com:8080/catalog/schema'
            driver = 'trino'
            user = 'your-username'
            // JWT token should be passed as a connection property
            properties = [
                accessToken: 'your-jwt-token'
            ]
        }
    }
}
```

### Pipeline Examples

#### Basic Query

Execute a simple query against your Trino cluster:

```nextflow
include { fromQuery } from 'plugin/nf-sqldb'

def sqlQuery = """
    SELECT customer_id, order_date, total_amount
    FROM orders
    WHERE order_date >= DATE '2023-01-01'
    LIMIT 100
    """

Channel.fromQuery(sqlQuery, db: 'trino').view()
```

#### Parameterized Query

Use parameters in your queries for dynamic data selection:

```nextflow
params {
    start_date = '2023-01-01'
    customer_region = 'US'
    limit = 50
}

def sqlQuery = """
    SELECT c.customer_name, o.order_id, o.total_amount
    FROM customers c
    JOIN orders o ON c.customer_id = o.customer_id
    WHERE o.order_date >= DATE '${params.start_date}'
      AND c.region = '${params.customer_region}'
    ORDER BY o.total_amount DESC
    LIMIT ${params.limit}
    """

Channel.fromQuery(sqlQuery, db: 'trino')
    .map { row -> 
        [customer_name: row[0], order_id: row[1], amount: row[2]]
    }
    .view()
```

#### Cross-Catalog Query

Query across multiple catalogs (e.g., combining data from different sources):

```nextflow
def sqlQuery = """
    SELECT 
        h.customer_id,
        h.event_time,
        c.customer_name,
        c.region
    FROM hive.analytics.user_events h
    JOIN postgresql.crm.customers c 
        ON h.customer_id = c.id
    WHERE h.event_time >= TIMESTAMP '2023-01-01 00:00:00'
    ORDER BY h.event_time DESC
    LIMIT 1000
    """

Channel.fromQuery(sqlQuery, db: 'trino')
    .groupTuple(by: 3)  // Group by region
    .view()
```

## Connection Parameters

The plugin supports all standard Trino JDBC connection parameters. Common parameters include:

| Parameter | Description | Example |
|-----------|-------------|---------|
| `url` | JDBC connection URL | `jdbc:trino://host:port/catalog/schema` |
| `user` | Username for authentication | `alice` |
| `password` | Password for LDAP authentication | `secret123` |
| `SSL` | Enable SSL/TLS connections | `true` |
| `accessToken` | JWT token for authentication | `eyJhbGciOiJSUzI1NiIs...` |
| `sessionUser` | Session username for impersonation | `bob` |
| `source` | Source name for query identification | `nextflow-pipeline` |
| `clientTags` | Tags for resource group selection | `batch,analytics` |

### Advanced Configuration

For complex scenarios, you can specify additional properties:

```nextflow config
sql {
    db {
        trino {
            url = 'jdbc:trino://trino-coordinator.example.com:8080/hive/default'
            driver = 'trino'
            user = 'analytics-user'
            properties = [
                SSL: 'true',
                SSLVerification: 'FULL',
                source: 'nextflow-analytics',
                clientTags: 'batch,genomics',
                sessionProperties: 'query_max_memory=10GB;query_max_run_time=1h',
                roles: 'hive:analyst;system:monitor'
            ]
        }
    }
}
```

## Troubleshooting

### Common Issues

1. **Connection Timeout**: Ensure network connectivity to the Trino coordinator
2. **Authentication Failed**: Verify username/password or JWT token validity
3. **Permission Denied**: Check that the user has access to the `system.jdbc` schema
4. **SSL Certificate Issues**: Use `SSLVerification=NONE` for testing (not recommended for production)

### Testing Connection

You can test your connection with a simple query:

```nextflow
def testQuery = "SELECT 'Connection successful' as status"
Channel.fromQuery(testQuery, db: 'trino').view()
```

## Performance Considerations

- Use `LIMIT` clauses for large result sets
- Consider using Trino's built-in spooling protocol for high-throughput queries
- Enable compression for better network performance: `disableCompression=false`
- Use appropriate session properties for memory and time limits

For more detailed information, refer to the [official Trino JDBC documentation](https://trino.io/docs/current/client/jdbc.html). 