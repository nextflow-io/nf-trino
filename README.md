# nf-trino plugin

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

## Testing with Nextflow

The plugin can be tested without a local Nextflow installation:

1. Build and install the plugin to your local Nextflow installation: `make install`
2. Run a pipeline with the plugin: `nextflow run hello -plugins nf-trino@0.1.0`

## Publishing

Plugins can be published to a central plugin registry to make them accessible to the Nextflow community. 


Follow these steps to publish the plugin to the Nextflow Plugin Registry:

1. Create a file named `$HOME/.gradle/gradle.properties`, where $HOME is your home directory. Add the following properties:

    * `pluginRegistry.accessToken`: Your Nextflow Plugin Registry access token. 

2. Use the following command to package and create a release for your plugin on GitHub: `make release`.


> [!NOTE]
> The Nextflow Pluging registry is currently avaialable as private beta technology. Contact info@nextflow.io to learn how to get access to it.
> 