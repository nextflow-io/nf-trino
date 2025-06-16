#!/usr/bin/env nextflow

/*
 * Starburst Example Pipeline
 * 
 * This example demonstrates how to use the nf-trino plugin to connect to
 * Starburst Galaxy or Starburst Enterprise and execute SQL queries.
 */

nextflow.enable.dsl = 2

include { fromQuery } from 'plugin/nf-sqldb'

// Parameters
params.db_name = 'starburst'
params.catalog = 'your_catalog'
params.schema = 'your_schema'
params.table = 'your_table'
params.limit = 10

workflow {
    log.info """
    Starburst Example Pipeline
    ==========================
    Database    : ${params.db_name}
    Catalog     : ${params.catalog}
    Schema      : ${params.schema}
    Table       : ${params.table}
    Limit       : ${params.limit}
    """
    // Test connection with a simple query
    def testQuery = "SELECT 'Starburst connection successful!' as status"
    
    Channel.fromQuery(testQuery, db: params.db_name)
        .view { "Connection test: ${it.status}" }
    
    // Show available catalogs
    def catalogQuery = "SHOW CATALOGS"
    
    Channel.fromQuery(catalogQuery, db: params.db_name)
        .view { "Available catalog: ${it.catalog_name}" }
    
    // Example data query (customize based on your data)
    def dataQuery = """
        SELECT *
        FROM ${params.catalog}.${params.schema}.${params.table}
        LIMIT ${params.limit}
        """
    
    Channel.fromQuery(dataQuery, db: params.db_name)
        .view { "Data row: ${it}" }
    
    // Example aggregation query
    def aggregationQuery = """
        SELECT 
            COUNT(*) as total_rows,
            'Summary from Starburst' as source
        FROM ${params.catalog}.${params.schema}.${params.table}
        """
    
    Channel.fromQuery(aggregationQuery, db: params.db_name)
        .view { "Summary: ${it.total_rows} rows found via ${it.source}" }
} 