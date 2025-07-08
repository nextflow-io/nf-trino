#!/usr/bin/env nextflow

/**
 * Example: Querying NIH SRA Athena Database
 * 
 * This example demonstrates how to connect to and query the NIH SRA (Sequence Read Archive)
 * Athena database using the nf-trino plugin following the established patterns.
 * 
 * Requirements:
 * - AWS credentials configured
 * - S3 bucket for Athena query results (in us-east-1 region)
 * - AWS Glue database with SRA metadata tables
 * 
 * Documentation: https://www.ncbi.nlm.nih.gov/sra/docs/sra-athena/
 * Tutorial: https://www.youtube.com/watch?v=_F4FhcDWSJg&ab_channel=TheNationalLibraryofMedicine
 */

nextflow.enable.dsl=2

include { fromQuery } from 'plugin/nf-sqldb'

// Parameters
params.aws_glue_db = 'sra_metadata_us_east_1'
params.aws_glue_db_table = 'metadata'
params.organism = 'Homo sapiens'
params.assay_type = 'WGS'
params.query_limit = 100

workflow {
    // SQL Queries
    def sraMetadataQuery = """
        SELECT 
            acc,
            assay_type,
            center_name,
            instrument,
            platform,
            organism,
            bioproject,
            spots,
            bases
        FROM "${params.aws_glue_db}".${params.aws_glue_db_table}
        WHERE assay_type = '${params.assay_type}'
          AND organism LIKE '%${params.organism}%'
          AND spots > 1000000
        ORDER BY bases DESC
        LIMIT ${params.query_limit};
    """

    def taxonomyQuery = """
        SELECT 
            ta.acc,
            ta.tax_id,
            ta.name,
            ta.abundance,
            t.rank,
            t.name_txt as taxonomic_name
        FROM "${params.aws_glue_db}".tax_analysis ta
        JOIN "${params.aws_glue_db}".taxonomy t ON ta.tax_id = t.tax_id
        WHERE ta.abundance > 0.1
          AND t.rank = 'species'
        ORDER BY ta.abundance DESC
        LIMIT ${params.query_limit};
    """

    def tableListQuery = """
        SELECT table_name, table_type 
        FROM information_schema.tables 
        WHERE table_schema = '${params.aws_glue_db}'
        ORDER BY table_name;
    """
    log.info """
    NIH SRA Athena Query Pipeline
    =============================
    AWS Glue DB   : ${params.aws_glue_db}
    Table         : ${params.aws_glue_db_table}
    Organism      : ${params.organism}
    Assay Type    : ${params.assay_type}
    Query Limit   : ${params.query_limit}
    """
    
    // Query available tables
    log.info "Querying available tables..."
    Channel.fromQuery(tableListQuery, db: 'awsathena')
        .view { "Available table: ${it[0]} (${it[1]})" }
    
    // Query SRA metadata
    log.info "Querying SRA metadata for ${params.organism} ${params.assay_type} samples..."
    Channel.fromQuery(sraMetadataQuery, db: 'awsathena')
        .view { row ->
            "SRA Record: ${row[0]} | ${row[1]} | ${row[4]} | ${row[5]} | ${row[7]} spots | ${row[8]} bases"
        }
        .collectFile(name: 'sra_metadata.csv', storeDir: 'results') { row ->
            "${row[0]},${row[1]},${row[2]},${row[3]},${row[4]},${row[5]},${row[6]},${row[7]},${row[8]}\n"
        }
    
    // Query taxonomy data if available
    log.info "Querying taxonomy analysis data..."
    Channel.fromQuery(taxonomyQuery, db: 'awsathena')
        .view { row ->
            "Taxonomy: ${row[0]} | ${row[2]} (${row[5]}) | Abundance: ${row[3]}"
        }
        .collectFile(name: 'taxonomy_analysis.csv', storeDir: 'results') { row ->
            "${row[0]},${row[1]},${row[2]},${row[3]},${row[4]},${row[5]}\n"
        }
}

workflow.onComplete {
    log.info """
    Pipeline completed!
    ===================
    Status    : ${workflow.success ? 'SUCCESS' : 'FAILED'}
    Work dir  : ${workflow.workDir}
    Results   : results/
    Duration  : ${workflow.duration}
    """
    
    if (!workflow.success) {
        log.error """
        Pipeline failed. Please check:
        - AWS credentials are properly configured
        - S3 bucket exists and is accessible in us-east-1 region
        - AWS Glue database '${params.aws_glue_db}' exists and has been crawled
        - Athena workgroup has proper permissions
        - Network access to AWS services is available
        """
    }
} 