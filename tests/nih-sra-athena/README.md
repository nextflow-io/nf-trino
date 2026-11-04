---
purpose: Demonstrate NIH SRA queries through AWS Athena.
applies_to: NIH SRA Athena example users
entrypoint: main.nf
verification: nf-test test main.nf.test
update_when: NIH SRA queries or Athena configuration change
---

# NIH SRA Athena Pipeline

This comprehensive example demonstrates advanced NIH SRA Athena querying with multiple data sources and result export.

## Description

This pipeline shows how to:
- Query NIH SRA metadata tables with complex filtering
- Access taxonomy analysis data
- Export results to CSV files
- Handle multiple related queries
- Use information schema for database introspection

## Requirements

- AWS credentials configured
- S3 bucket for Athena query results in the `us-east-1` region
- AWS Glue database `sra_metadata_us_east_1` with the following tables:
  - `metadata`: Main SRA metadata
  - `tax_analysis`: Taxonomy analysis results
  - `taxonomy`: Taxonomic classifications

## Configuration

Edit the `nextflow.config` file to set your AWS credentials:

```groovy
sql {
    db {
        awsathena {
            url = 'jdbc:athena://Region=us-east-1;OutputLocation=s3://your-athena-results-bucket/query-results/;CredentialsProvider=DefaultChain;'
        }
    }
}
```

## Usage

```bash
# Run with default parameters (Homo sapiens WGS)
nextflow run main.nf

# Run with different organism and assay type
nextflow run main.nf \
    --organism 'Mus musculus' \
    --assay_type 'RNA-Seq' \
    --query_limit 50

# Run with custom database
nextflow run main.nf \
    --aws_glue_db 'custom_sra_db' \
    --organism 'Escherichia coli' \
    --assay_type 'WGS'
```

## Parameters

- `aws_glue_db`: AWS Glue database name (default: 'sra_metadata_us_east_1')
- `aws_glue_db_table`: Main metadata table name (default: 'metadata')
- `organism`: Organism to filter by (default: 'Homo sapiens')
- `assay_type`: Assay type to filter by (default: 'WGS')
- `query_limit`: Maximum number of results per query (default: 100)

## Testing

Run the nf-test suite:

```bash
nf-test test main.nf.test
```

## Expected Output

The pipeline executes three main queries and creates two output files:

### Console Output
```
NIH SRA Athena Query Pipeline
=============================
AWS Glue DB   : sra_metadata_us_east_1
Table         : metadata
Organism      : Homo sapiens
Assay Type    : WGS
Query Limit   : 100

Querying available tables...
Available table: metadata (BASE TABLE)
Available table: tax_analysis (BASE TABLE)
Available table: taxonomy (BASE TABLE)

Querying SRA metadata for Homo sapiens WGS samples...
SRA Record: SRR123456 | WGS | ILLUMINA | Homo sapiens | 5000000 spots | 750000000 bases
...

Querying taxonomy analysis data...
Taxonomy: SRR123456 | Homo sapiens (Homo sapiens) | Abundance: 0.95
...

Pipeline completed!
===================
Status    : SUCCESS
Work dir  : /tmp/nextflow-work
Results   : results/
Duration  : 2m 30s
```

### Output Files

#### `results/sra_metadata.csv`
```csv
SRR123456,WGS,NCBI,Illumina HiSeq 2000,ILLUMINA,Homo sapiens,PRJNA12345,5000000,750000000
SRR123457,WGS,EBI,Illumina HiSeq 4000,ILLUMINA,Homo sapiens,PRJNA12346,6000000,900000000
...
```

#### `results/taxonomy_analysis.csv`
```csv
SRR123456,9606,Homo sapiens,0.95,species,Homo sapiens
SRR123457,9606,Homo sapiens,0.98,species,Homo sapiens
...
```

## Queries Executed

### 1. Available Tables Query
Lists all tables in the specified AWS Glue database to verify data availability.

### 2. SRA Metadata Query
```sql
SELECT 
    acc, assay_type, center_name, instrument, platform, organism, bioproject, spots, bases
FROM "sra_metadata_us_east_1".metadata
WHERE assay_type = 'WGS'
  AND organism LIKE '%Homo sapiens%'
  AND spots > 1000000
ORDER BY bases DESC
LIMIT 100;
```

### 3. Taxonomy Analysis Query
```sql
SELECT 
    ta.acc, ta.tax_id, ta.name, ta.abundance, t.rank, t.name_txt as taxonomic_name
FROM "sra_metadata_us_east_1".tax_analysis ta
JOIN "sra_metadata_us_east_1".taxonomy t ON ta.tax_id = t.tax_id
WHERE ta.abundance > 0.1
  AND t.rank = 'species'
ORDER BY ta.abundance DESC
LIMIT 100;
```

## Error Handling

The pipeline includes comprehensive error handling and will provide detailed guidance if queries fail:

```
Pipeline failed. Please check:
- AWS credentials are properly configured
- S3 bucket exists and is accessible in us-east-1 region
- AWS Glue database 'sra_metadata_us_east_1' exists and has been crawled
- Athena workgroup has proper permissions
- Network access to AWS services is available
```

## Related Documentation

- [NIH SRA Athena Documentation](https://www.ncbi.nlm.nih.gov/sra/docs/sra-athena/)
- [NIH SRA Athena Tutorial Video](https://www.youtube.com/watch?v=_F4FhcDWSJg)
- [AWS Athena User Guide](https://docs.aws.amazon.com/athena/latest/ug/)