#!/usr/bin/env nextflow

/*
 * Example pipeline demonstrating AWS Athena integration
 */

nextflow.enable.dsl = 2

params.aws_glue_db = 'sra-glue-db'
params.aws_glue_db_table = 'metadata'
params.organism = 'Mycobacterium tuberculosis'
params.limit = 10

include { fromQuery } from 'plugin/nf-trino'

workflow {
    def sqlQuery = """
        SELECT *
        FROM \"${params.aws_glue_db}\".${params.aws_glue_db_table}
        WHERE organism = '${params.organism}'
        LIMIT ${params.limit};
        """
    
    Channel.fromQuery(sqlQuery, db: 'awsathena')
        .view { row -> 
            "Found record: ${row[0]} - ${row[13]}" // SRR ID and organism
        }
} 