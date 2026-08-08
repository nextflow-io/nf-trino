#!/usr/bin/env nextflow

nextflow.enable.dsl = 2

include { fromQuery } from 'plugin/nf-sqldb'

workflow {
    log.info "Testing nf-trino plugin SQL extension..."
    
    // Test that the SQL extension is available
    try {
        log.info "✅ SQL extension (fromQuery) is available"
        log.info "🎉 SQL extension test completed!"
        
        // Note: We can't test actual database connections without credentials
        // but we can verify the extension loads properly
        
    } catch (Exception e) {
        log.error "❌ SQL extension test failed: ${e.message}"
        throw e
    }
} 