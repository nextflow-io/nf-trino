# Simple SRA Query Pipeline

This example demonstrates a basic NIH SRA Athena query using the nf-trino plugin following established patterns.

## Description

This pipeline shows how to:
- Query NIH SRA metadata using the nf-trino plugin
- Use the `fromQuery` function from the SQL extension
- Filter results by organism
- Handle AWS Athena configuration
- Log query results with proper error handling

## Requirements

- AWS credentials configured
- S3 bucket for Athena query results in the `us-east-1` region
- AWS Glue database `sra_metadata_us_east_1` set up

## Configuration

Edit the `nextflow.config` file to set your AWS credentials:

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
# Run with default parameters (Mycobacterium tuberculosis)
nextflow run main.nf

# Run with custom organism
nextflow run main.nf --organism 'Homo sapiens'

# Run with custom database and parameters
nextflow run main.nf \
    --aws_glue_db 'your_database' \
    --organism 'Mus musculus' \
    --limit 50

# Run with all custom parameters
nextflow run main.nf \
    --aws_glue_db 'custom_sra_db' \
    --aws_glue_db_table 'custom_metadata' \
    --organism 'Escherichia coli' \
    --limit 100
```

## Parameters

- `aws_glue_db`: AWS Glue database name (default: 'sra_metadata_us_east_1')
- `aws_glue_db_table`: Table name in the database (default: 'metadata')
- `organism`: Organism to filter by (default: 'Mycobacterium tuberculosis')
- `limit`: Maximum number of results (default: 10)

## Testing

Run the nf-test suite:

```bash
nf-test test main.nf.test
```

## Expected Output

The pipeline will print SRA records to the console and log completion status:

```
SRA Athena Query
================
Database: sra_metadata_us_east_1
Table: metadata
Organism: Mycobacterium tuberculosis
Limit: 10

SRA Record: SRR123456 | value1 | value2 | Organism: Mycobacterium tuberculosis
SRA Record: SRR123457 | value1 | value2 | Organism: Mycobacterium tuberculosis
...

Query completed: SUCCESS
```

## Error Handling

If the query fails, the pipeline will log helpful error messages:

```
Query completed: FAILED
Check your configuration:
- AWS credentials in nextflow.config
- S3 bucket permissions
- AWS Glue database 'sra_metadata_us_east_1' exists
```

## SQL Query Pattern

The pipeline uses the exact SQL pattern from the aws-athena documentation:

```sql
SELECT *
FROM "sra_metadata_us_east_1".metadata
WHERE organism = 'Mycobacterium tuberculosis'
LIMIT 10;
```

## Related Documentation

- [NIH SRA Athena Documentation](https://www.ncbi.nlm.nih.gov/sra/docs/sra-athena/)
- [AWS Athena SQL Reference](https://docs.aws.amazon.com/athena/latest/ug/language-reference.html)
- [nf-trino Plugin Documentation](../../README.md)