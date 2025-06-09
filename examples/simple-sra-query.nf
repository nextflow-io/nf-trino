#!/usr/bin/env nextflow

/**
 * Simple NIH SRA Athena Query Example
 * 
 * This example follows the established pattern from aws-athena.md
 * to query the NIH SRA metadata using AWS Athena.
 * 
 * Requirements:
 * - Configure nextflow.config with your AWS credentials and S3 bucket
 * - AWS Glue database 'sra_metadata_us_east_1' must be set up
 * 
 * Reference: https://www.ncbi.nlm.nih.gov/sra/docs/sra-athena/
 * Tutorial: https://www.youtube.com/watch?v=_F4FhcDWSJg
 */

nextflow.enable.dsl=2

include { fromQuery } from 'plugin/nf-sqldb'

// Parameters (can be overridden in nextflow.config or command line)
params.aws_glue_db = 'sra_metadata_us_east_1'
params.aws_glue_db_table = 'metadata'
params.organism = 'Mycobacterium tuberculosis'
params.limit = 10

workflow {
    log.info """
    SRA Athena Query
    ================
    Database: ${params.aws_glue_db}
    Table: ${params.aws_glue_db_table}
    Organism: ${params.organism}
    Limit: ${params.limit}
    """

    // Query following the exact pattern from aws-athena.md
    def sqlQuery = """
        SELECT *
        FROM "${params.aws_glue_db}".${params.aws_glue_db_table}
        WHERE organism = '${params.organism}'
        LIMIT ${params.limit};
    """

    Channel.fromQuery(sqlQuery, db: 'awsathena')
        .view { row ->
            log.info "SRA Record: ${row[0]} | ${row[1]} | ${row[2]} | Organism: ${row[14]}"
            return row
        }
}

workflow.onComplete {
    log.info "Query completed: ${workflow.success ? 'SUCCESS' : 'FAILED'}"
    if (!workflow.success) {
        log.error """
        Check your configuration:
        - AWS credentials in nextflow.config
        - S3 bucket permissions
        - AWS Glue database '${params.aws_glue_db}' exists
        """
    }
} 