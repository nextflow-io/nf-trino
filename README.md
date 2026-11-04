# nf-trino

## Summary

`nf-trino` adds Trino, Starburst, and AWS Athena JDBC drivers to Nextflow's
`nf-sqldb` extension. Use it with `Channel.fromQuery` to stream SQL query
results into a workflow.

## Get started

Install the published plugin in `nextflow.config`:

```nextflow
plugins {
    id 'nf-trino@<version>'
}

sql {
    db {
        starburst {
            url = 'jdbc:trino://your-starburst-host:443/catalog/schema?SSL=true'
            user = 'your-username'
            password = secrets.STARBURST_PASSWORD
        }
    }
}
```


Then query it from a workflow:

```nextflow
include { fromQuery } from 'plugin/nf-trino'

workflow {
    Channel
        .fromQuery("SELECT 'connection successful' AS status", db: 'starburst')
        .view()
}
```

For local development, run `make install`, then invoke Nextflow with
`-plugins nf-trino@0.1.0`.

## Examples

Query Trino and stream rows into a process:

```nextflow
include { fromQuery } from 'plugin/nf-trino'

workflow {
    Channel
        .fromQuery(
            "SELECT sample_id, file_path FROM genomics.samples WHERE status = 'ready'",
            db: 'trino')
        .map { row -> tuple(row.sample_id, file(row.file_path)) }
        .view()
}
```

Query AWS Athena using the default credentials chain:

```nextflow config
sql {
    db {
        athena {
            url = 'jdbc:athena://Region=us-east-1;OutputLocation=s3://your-bucket/results/;CredentialsProvider=DefaultChain;'
        }
    }
}
```

```nextflow
include { fromQuery } from 'plugin/nf-trino'

workflow {
    Channel
        .fromQuery('SELECT run_accession FROM sra.metadata LIMIT 10', db: 'athena')
        .view()
}
```

The [`tests/`](tests) directory provides runnable Trino, Starburst, and
Athena configurations. See the dedicated guides for
[Trino](docs/trino.md), [Starburst](docs/starburst.md), and
[AWS Athena](docs/aws-athena.md).

## Development

```bash
make test                    # deterministic unit tests
make smoke                   # clean-install fromQuery smoke test
./gradlew test -Pintegration # Docker and credential-backed integration tests
make assemble
```

The integration suite needs Docker. Athena tests additionally require
`ATHENA_TEST_S3_OUTPUT_LOCATION` to name a writable S3 result location.

`make release` publishes the configured version to the Nextflow plugin
registry. Before releasing, claim `nf-trino` in the registry and configure the
registry API key as described in the
[Nextflow publishing guide](https://docs.seqera.io/nextflow/guides/gradle-plugin#publishing-a-plugin).

## License

This project is licensed under the Apache License 2.0. See [COPYING](COPYING).
