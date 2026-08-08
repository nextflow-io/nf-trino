#!/usr/bin/env nextflow

/**
 * Simple NIH SRA Athena Connection Test
 * 
 * This example demonstrates how to test connection to the NIH SRA Athena database.
 * 
 * Requirements:
 * - AWS credentials configured
 * - S3 bucket for query results in us-east-1 region
 * 
 * Usage:
 *   nextflow run main.nf --s3_bucket s3://your-bucket/results/
 */

nextflow.enable.dsl=2

params.s3_bucket = null
params.query_limit = 10

process TEST_ATHENA_CONNECTION {
    publishDir 'results', mode: 'copy'
    
    output:
    path "athena_test_results.txt"
    
    script:
    """
    #!/usr/bin/env uv run --script
    # /// script
    # dependencies = [
    #   "boto3==1.34.0",
    #   "pandas==2.1.0"
    # ]
    # ///
    
    import boto3
    import json
    from datetime import datetime
    
    def test_athena_connection():
        try:
            # Initialize Athena client
            athena_client = boto3.client('athena', region_name='us-east-1')
            
            # Test query on SRA metadata
            query = '''
                SELECT table_name, table_type 
                FROM information_schema.tables 
                WHERE table_schema = 'sra_metadata_us_east_1'
                LIMIT 5
            '''
            
            # Submit query
            response = athena_client.start_query_execution(
                QueryString=query,
                QueryExecutionContext={'Database': 'sra_metadata_us_east_1'},
                ResultConfiguration={'OutputLocation': '${params.s3_bucket}'}
            )
            
            query_id = response['QueryExecutionId']
            
            # Wait for query completion (simplified check)
            import time
            for _ in range(30):  # Wait up to 30 seconds
                status = athena_client.get_query_execution(QueryExecutionId=query_id)
                state = status['QueryExecution']['Status']['State']
                if state in ['SUCCEEDED', 'FAILED', 'CANCELLED']:
                    break
                time.sleep(1)
            
            results = {
                'timestamp': datetime.now().isoformat(),
                'query_id': query_id,
                'status': state,
                'database': 'sra_metadata_us_east_1',
                'region': 'us-east-1',
                'success': state == 'SUCCEEDED'
            }
            
            if state == 'SUCCEEDED':
                # Get results
                result_response = athena_client.get_query_results(QueryExecutionId=query_id)
                results['row_count'] = len(result_response['ResultSet']['Rows'])
                results['tables_found'] = [
                    row['Data'][0].get('VarCharValue', '') 
                    for row in result_response['ResultSet']['Rows'][1:]  # Skip header
                    if len(row['Data']) > 0
                ]
            
            return results
            
        except Exception as e:
            return {
                'timestamp': datetime.now().isoformat(),
                'error': str(e),
                'success': False
            }
    
    # Run the test
    test_results = test_athena_connection()
    
    # Write results
    with open('athena_test_results.txt', 'w') as f:
        f.write("NIH SRA Athena Connection Test\\n")
        f.write("=" * 40 + "\\n\\n")
        f.write(f"Timestamp: {test_results['timestamp']}\\n")
        f.write(f"Success: {test_results['success']}\\n\\n")
        
        if test_results['success']:
            f.write(f"Query ID: {test_results['query_id']}\\n")
            f.write(f"Database: {test_results['database']}\\n")
            f.write(f"Region: {test_results['region']}\\n")
            f.write(f"Tables Found: {len(test_results.get('tables_found', []))}\\n\\n")
            
            if 'tables_found' in test_results:
                f.write("Available Tables:\\n")
                for table in test_results['tables_found']:
                    f.write(f"  - {table}\\n")
        else:
            f.write(f"Error: {test_results.get('error', 'Unknown error')}\\n")
    
    echo "Athena connection test completed. Check athena_test_results.txt for details."
    """
}

process QUERY_SRA_SAMPLE {
    publishDir 'results', mode: 'copy'
    
    output:
    path "sra_sample_data.csv"
    
    when:
    params.s3_bucket
    
    script:
    """
    #!/usr/bin/env uv run --script
    # /// script
    # dependencies = [
    #   "boto3==1.34.0",
    #   "pandas==2.1.0"
    # ]
    # ///
    
    import boto3
    import pandas as pd
    import time
    
    def query_sra_metadata():
        try:
            athena_client = boto3.client('athena', region_name='us-east-1')
            
            # Query for WGS samples from Homo sapiens
            query = '''
                SELECT acc, assay_type, center_name, instrument, platform, organism
                FROM sra_metadata_us_east_1.metadata 
                WHERE assay_type = 'WGS' 
                  AND organism LIKE '%Homo sapiens%'
                LIMIT ${params.query_limit}
            '''
            
            response = athena_client.start_query_execution(
                QueryString=query,
                QueryExecutionContext={'Database': 'sra_metadata_us_east_1'},
                ResultConfiguration={'OutputLocation': '${params.s3_bucket}'}
            )
            
            query_id = response['QueryExecutionId']
            
            # Wait for completion
            for _ in range(60):
                status = athena_client.get_query_execution(QueryExecutionId=query_id)
                state = status['QueryExecution']['Status']['State']
                if state in ['SUCCEEDED', 'FAILED', 'CANCELLED']:
                    break
                time.sleep(1)
            
            if state == 'SUCCEEDED':
                # Get results and convert to DataFrame
                result_response = athena_client.get_query_results(QueryExecutionId=query_id)
                
                # Extract data
                rows = result_response['ResultSet']['Rows']
                headers = [col['VarCharValue'] for col in rows[0]['Data']]
                data = []
                
                for row in rows[1:]:  # Skip header row
                    data.append([col.get('VarCharValue', '') for col in row['Data']])
                
                df = pd.DataFrame(data, columns=headers)
                df.to_csv('sra_sample_data.csv', index=False)
                
                print(f"Successfully retrieved {len(df)} records")
                return True
            else:
                print(f"Query failed with state: {state}")
                return False
                
        except Exception as e:
            print(f"Error: {e}")
            return False
    
    # Run the query
    success = query_sra_metadata()
    
    if not success:
        # Create empty CSV with headers if query failed
        import pandas as pd
        df = pd.DataFrame(columns=['acc', 'assay_type', 'center_name', 'instrument', 'platform', 'organism'])
        df.to_csv('sra_sample_data.csv', index=False)
    """
}

workflow {
    if (!params.s3_bucket) {
        error "Please provide S3 bucket: --s3_bucket s3://your-bucket/results/"
    }
    
    log.info """
    NIH SRA Athena Test
    ===================
    S3 Bucket: ${params.s3_bucket}
    Query Limit: ${params.query_limit}
    """
    
    TEST_ATHENA_CONNECTION()
    QUERY_SRA_SAMPLE()
}

workflow.onComplete {
    log.info "Test completed: ${workflow.success ? 'SUCCESS' : 'FAILED'}"
} 