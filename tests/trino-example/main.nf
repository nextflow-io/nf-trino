#!/usr/bin/env nextflow

/*
 * Trino Example Pipeline
 * 
 * This example demonstrates how to use the nf-trino plugin to query
 * a Trino cluster directly from Nextflow workflows.
 * 
 * Note: Make sure to configure the Trino database connection in your
 * nextflow.config file before running this example.
 */

nextflow.enable.dsl = 2

params.trino_catalog = 'hive'
params.trino_schema = 'default'
params.limit = 10

include { fromQuery } from 'plugin/nf-trino'

workflow {
    // Simple connection test query
    def testQuery = "SELECT 'Trino connection successful' as status, current_timestamp as timestamp"
    
    // Query to show available schemas
    def schemasQuery = """
        SELECT catalog_name, schema_name, schema_owner
        FROM information_schema.schemata
        WHERE catalog_name = '${params.trino_catalog}'
        LIMIT ${params.limit}
    """
    
    // Query to show available tables
    def tablesQuery = """
        SELECT table_catalog, table_schema, table_name, table_type
        FROM information_schema.tables
        WHERE table_catalog = '${params.trino_catalog}'
        AND table_schema = '${params.trino_schema}'
        LIMIT ${params.limit}
    """
    
    // Execute queries and process results
    Channel.fromQuery(testQuery, db: 'trino')
        .view { row -> 
            "✅ Connection Test: ${row[0]} at ${row[1]}"
        }
    
    Channel.fromQuery(schemasQuery, db: 'trino')
        .view { row ->
            "📋 Schema: ${row[0]}.${row[1]} (owner: ${row[2]})"
        }
    
    Channel.fromQuery(tablesQuery, db: 'trino')
        .view { row ->
            "📊 Table: ${row[0]}.${row[1]}.${row[2]} (type: ${row[3]})"
        }
} 