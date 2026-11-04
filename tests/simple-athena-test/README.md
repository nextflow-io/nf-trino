# Simple Athena Test Pipeline

This example demonstrates how to test AWS Athena connectivity using Python scripts within Nextflow processes.

## Description

This pipeline shows how to:
- Test AWS Athena connection using boto3
- Query NIH SRA metadata tables
- Handle AWS credentials and S3 configuration
- Use Python scripts within Nextflow processes
- Generate test results and sample data files

## Requirements

- AWS credentials configured (via AWS CLI, environment variables, or IAM roles)
- S3 bucket for Athena query results in the `us-east-1` region
- AWS Glue database `sra_metadata_us_east_1` set up
- Python dependencies (boto3, pandas) - handled by uv

## Configuration

This pipeline uses environment variables for AWS credentials. You can set them in your shell:

```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_DEFAULT_REGION=us-east-1
```

Or configure them in your `~/.aws/credentials` file.

## Usage

```bash
# Run with S3 bucket parameter (required)
nextflow run main.nf --s3_bucket s3://your-bucket/results/

# Run with custom query limit
nextflow run main.nf \
    --s3_bucket s3://your-bucket/results/ \
    --query_limit 50

# The pipeline will fail if no S3 bucket is provided
nextflow run main.nf  # This will fail with error message
```

## Parameters

- `s3_bucket`: S3 bucket URL for Athena query results (required)
- `query_limit`: Maximum number of results to retrieve (default: 10)

## Testing

Run the nf-test suite:

```bash
nf-test test main.nf.test
```

## Expected Output

The pipeline creates two output files in the `results/` directory:

### 1. `athena_test_results.txt`
```
NIH SRA Athena Connection Test
========================================

Timestamp: 2024-01-15T10:30:45.123456
Success: True

Query ID: 12345678-1234-1234-1234-123456789012
Database: sra_metadata_us_east_1
Region: us-east-1
Tables Found: 3

Available Tables:
  - metadata
  - tax_analysis
  - taxonomy
```

### 2. `sra_sample_data.csv`
```csv
acc,assay_type,center_name,instrument,platform,organism
SRR123456,WGS,NCBI,Illumina HiSeq 2000,ILLUMINA,Homo sapiens
SRR123457,WGS,EBI,Illumina HiSeq 4000,ILLUMINA,Homo sapiens
...
```

## Processes

### TEST_ATHENA_CONNECTION
- Tests basic connectivity to AWS Athena
- Queries information schema to verify database access
- Generates detailed test results

### QUERY_SRA_SAMPLE
- Queries actual SRA metadata for WGS samples
- Filters for Homo sapiens samples
- Exports results to CSV format

## Error Handling

The pipeline includes comprehensive error handling:
- Validates S3 bucket parameter
- Handles AWS authentication errors
- Manages query timeout scenarios
- Creates empty output files if queries fail