# Athena Example Pipeline

This example demonstrates basic AWS Athena integration using the nf-trino plugin.

## Description

This pipeline shows how to:
- Connect to AWS Athena using the nf-trino plugin
- Query AWS Glue databases
- Filter results by organism
- Use the `fromQuery` function from the SQL extension

## Requirements

- AWS credentials configured (via AWS CLI, environment variables, or IAM roles)
- S3 bucket for Athena query results in the `us-east-1` region
- AWS Glue database with SRA metadata tables

## Configuration

Edit the `nextflow.config` file to set your AWS credentials and S3 bucket:

```groovy
sql {
    db {
        awsathena {
            url = 'jdbc:awsathena://AwsRegion=us-east-1;S3OutputLocation=s3://your-athena-results-bucket/query-results/'
            user = 'YOUR_AWS_ACCESS_KEY'
            password = 'YOUR_AWS_SECRET_KEY'
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
    --aws_glue_db 'your_database' \
    --organism 'Homo sapiens' \
    --limit 50

# Run with plugin version
nextflow run main.nf -plugins nf-trino@0.1.0
```

## Parameters

- `aws_glue_db`: AWS Glue database name (default: 'sra-glue-db')
- `aws_glue_db_table`: Table name in the database (default: 'metadata')
- `organism`: Organism to filter by (default: 'Mycobacterium tuberculosis')
- `limit`: Maximum number of results (default: 10)

## Testing

Run the nf-test suite:

```bash
nf-test test main.nf.test
```

## Expected Output

The pipeline will print found records to the console:

```
Found record: SRR123456 - Mycobacterium tuberculosis
Found record: SRR123457 - Mycobacterium tuberculosis
...
```