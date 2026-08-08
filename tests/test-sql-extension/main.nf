#!/usr/bin/env nextflow

nextflow.enable.dsl = 2

include { fromQuery } from 'plugin/nf-trino'

workflow {
    Channel
        .fromQuery('SELECT 1 AS test_value')
        .map { row ->
            assert row == [1]
            'nf-trino fromQuery smoke: OK'
        }
        .view()
}