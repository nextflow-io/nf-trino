# NIH SRA Athena Integration Guide

This guide explains how to connect to and query the NIH SRA (Sequence Read Archive) Athena database using the `nf-trino` plugin, following the established patterns from the plugin documentation.

## Overview

The NIH provides public access to SRA metadata through AWS Athena, allowing researchers to query genomics metadata without downloading large datasets. This integration uses the existing `nf-sqldb` plugin functionality with the AWS Athena driver.

## Prerequisites

1. **AWS Account** with appropriate permissions for Athena and S3
2. **AWS Glue Database** - The `sra_metadata_us_east_1` database set up via AWS Glue crawler
3. **S3 Bucket** in `us-east-1` region for Athena query results
4. **AWS Credentials** configured (CLI, environment variables, or IAM role)

## Setup

### 1. Follow the NCBI Tutorial

Before using this integration, follow the [official NCBI tutorial](https://www.youtube.com/watch?v=_F4FhcDWSJg&ab_channel=TheNationalLibraryofMedicine) to:
- Set up AWS Glue crawler for SRA metadata
- Create the database and tables
- Configure proper IAM permissions

### 2. Configure Nextflow

Create a `nextflow.config` file with your AWS settings:

```groovy
params {
    aws_glue_db = 'sra_metadata_us_east_1'
    aws_glue_db_table = 'metadata'
    organism = 'Homo sapiens'
    assay_type = 'WGS'
    query_limit = 100
}

plugins {
    id 'nf-trino'
}

sql {
    db {
        awsathena {
            url = 'jdbc:awsathena://AwsRegion=us-east-1;S3OutputLocation=s3://your-athena-results-bucket/query-results/'
            user = '<YOUR_AWS_ACCESS_KEY>'
            password = '<YOUR_AWS_SECRET_KEY>'
        }
    }
}
```

## Basic Usage

Here's a simple example following the established pattern:

```groovy
#!/usr/bin/env nextflow

nextflow.enable.dsl=2

include { fromQuery } from 'plugin/nf-sqldb'

params.aws_glue_db = 'sra_metadata_us_east_1'
params.aws_glue_db_table = 'metadata'
params.organism = 'Mycobacterium tuberculosis'

workflow {
    def sqlQuery = """
        SELECT *
        FROM "${params.aws_glue_db}".${params.aws_glue_db_table}
        WHERE organism = '${params.organism}'
        LIMIT 10;
    """

    Channel.fromQuery(sqlQuery, db: 'awsathena').view()
}
```

## Available Tables

After setting up the AWS Glue crawler, you'll have access to these SRA tables:

- `metadata` - Core SRA metadata (accessions, instruments, organisms, etc.)
- `tax_analysis` - Taxonomic analysis results  
- `taxonomy` - Taxonomic hierarchy
- `kmer` - K-mer analysis data

## Example Queries

### Query by Organism and Assay Type

```sql
SELECT acc, assay_type, center_name, instrument, platform, organism, bioproject
FROM "sra_metadata_us_east_1".metadata 
WHERE assay_type = 'WGS' 
  AND organism LIKE '%Homo sapiens%'
LIMIT 100;
```

### Taxonomy Analysis

```sql
SELECT ta.acc, ta.tax_id, ta.name, ta.abundance, t.rank, t.name_txt
FROM "sra_metadata_us_east_1".tax_analysis ta
JOIN "sra_metadata_us_east_1".taxonomy t ON ta.tax_id = t.tax_id
WHERE ta.abundance > 0.1 AND t.rank = 'species'
ORDER BY ta.abundance DESC;
```

### List Available Tables

```sql
SELECT table_name, table_type 
FROM information_schema.tables 
WHERE table_schema = 'sra_metadata_us_east_1';
```

## Running the Examples

1. **Configure your environment**:
   ```bash
   # Set up your nextflow.config with AWS credentials
   cp examples/nextflow.config .
   # Edit with your AWS credentials and S3 bucket
   ```

2. **Run the simple example**:
   ```bash
   nextflow run examples/simple-sra-query.nf --organism "Homo sapiens"
   ```

3. **Run tests to verify connection**:
   ```bash
   # Run the plugin tests (requires AWS credentials)
   ./gradlew test
   ```

## Testing Connection

The plugin includes comprehensive tests in `NfTrinoPluginTest.groovy`:

```groovy
@Requires({ 
    System.getenv('AWS_ACCESS_KEY_ID') || System.getenv('AWS_PROFILE') || 
    new File(System.getProperty('user.home') + '/.aws/credentials').exists()
})
def 'should connect to NIH SRA Athena instance and query metadata'() {
    // Test connects to actual SRA Athena instance
    // Queries metadata tables and verifies results
}
```

Tests only run when AWS credentials are available and will:
- Connect to the SRA Athena instance
- Query available tables
- Retrieve sample metadata
- Test taxonomy analysis data

## Cost Considerations

- **Athena Queries**: Charged per TB of data scanned (~$5/TB)
- **S3 Storage**: Minimal cost for query results
- **AWS Free Tier**: Limited monthly queries available

## Troubleshooting

### Common Issues

1. **Connection Failed**: Check AWS credentials and permissions
2. **Database Not Found**: Ensure AWS Glue crawler has run successfully  
3. **S3 Access Denied**: Verify S3 bucket permissions and region (must be us-east-1)
4. **Query Timeout**: Large queries may need optimization or increased timeouts

### Required IAM Permissions

Your AWS user/role needs permissions for:
- `athena:StartQueryExecution`
- `athena:GetQueryExecution` 
- `athena:GetQueryResults`
- `glue:GetDatabase`
- `glue:GetTable`
- `s3:GetObject` and `s3:PutObject` on your results bucket

## References

- [NIH SRA Athena Documentation](https://www.ncbi.nlm.nih.gov/sra/docs/sra-athena/)
- [NCBI Setup Tutorial](https://www.youtube.com/watch?v=_F4FhcDWSJg&ab_channel=TheNationalLibraryofMedicine)
- [AWS Athena Documentation](https://docs.aws.amazon.com/athena/)
- [nf-trino Plugin AWS Athena Guide](../docs/aws-athena.md) 