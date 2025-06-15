/*
 * Copyright 2025, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.plugin

import groovy.sql.Sql
import nextflow.sql.config.DriverRegistry
import org.pf4j.PluginWrapper
import spock.lang.Specification
import spock.lang.Ignore
import spock.lang.Requires

/**
 * Test for NfTrinoPlugin
 */
class NfTrinoPluginTest extends Specification {

    def 'should register AWS Athena driver' () {
        given:
        def wrapper = Mock(PluginWrapper)
        
        when:
        new NfTrinoPlugin(wrapper)
        
        then:
        DriverRegistry.DEFAULT.getDrivers().containsKey("awsathena")
        DriverRegistry.DEFAULT.getDrivers()["awsathena"] == "com.simba.athena.jdbc.Driver"
    }

    def 'should register Trino driver' () {
        given:
        def wrapper = Mock(PluginWrapper)
        
        when:
        new NfTrinoPlugin(wrapper)
        
        then:
        DriverRegistry.DEFAULT.getDrivers().containsKey("trino")
        DriverRegistry.DEFAULT.getDrivers()["trino"] == "io.trino.jdbc.TrinoDriver"
    }

    @Requires({ 
        // Check if AWS credentials are available
        System.getenv('AWS_ACCESS_KEY_ID') || System.getenv('AWS_PROFILE') || 
        new File(System.getProperty('user.home') + '/.aws/credentials').exists()
    })
    def 'should connect to NIH SRA Athena instance and query metadata'() {
        given: 'NIH SRA Athena connection parameters'
        def athenaUrl = "jdbc:awsathena://AwsDataCatalog:sra_metadata_us_east_1@athena.us-east-1.amazonaws.com:443"
        def workgroupUrl = athenaUrl + ";Workgroup=primary;S3OutputLocation=s3://your-athena-results-bucket/query-results/"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Connecting to NIH SRA Athena'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.simba.athena.jdbc.Driver")
        
        then: 'Connection should be established'
        sql != null
        
        when: 'Querying SRA metadata tables'
        def tables = sql.rows("""
            SELECT table_name, table_type 
            FROM information_schema.tables 
            WHERE table_schema = 'sra_metadata_us_east_1'
            LIMIT 5
        """)
        
        then: 'Should return available tables'
        tables != null
        tables.size() > 0
        
        when: 'Querying sample metadata with limit'
        def sampleData = sql.rows("""
            SELECT acc, assay_type, center_name, instrument, librarylayout 
            FROM sra_metadata_us_east_1.metadata 
            WHERE assay_type = 'WGS'
            LIMIT 10
        """)
        
        then: 'Should return sample data'
        sampleData != null
        sampleData.size() > 0
        sampleData.each { row ->
            assert row.acc != null
            assert row.assay_type == 'WGS'
        }
        
        cleanup:
        sql?.close()
    }

    @Requires({ 
        // Check if AWS credentials are available
        System.getenv('AWS_ACCESS_KEY_ID') || System.getenv('AWS_PROFILE') || 
        new File(System.getProperty('user.home') + '/.aws/credentials').exists()
    })
    def 'should query NIH SRA SARS-CoV-2 specific dataset'() {
        given: 'NIH SRA SARS-CoV-2 Athena connection parameters'
        def athenaUrl = "jdbc:awsathena://AwsDataCatalog:sra_sars_cov_2_us_east_1@athena.us-east-1.amazonaws.com:443"
        def workgroupUrl = athenaUrl + ";Workgroup=primary;S3OutputLocation=s3://your-athena-results-bucket/query-results/"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Connecting to NIH SRA SARS-CoV-2 Athena'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.simba.athena.jdbc.Driver")
        
        then: 'Connection should be established'
        sql != null
        
        when: 'Querying available tables in SARS-CoV-2 dataset'
        def tables = sql.rows("""
            SELECT table_name, table_type 
            FROM information_schema.tables 
            WHERE table_schema = 'sra_sars_cov_2_us_east_1'
            LIMIT 5
        """)
        
        then: 'Should return available tables'
        tables != null
        tables.size() > 0
        
        when: 'Querying annotated variations if available'
        def variations = sql.rows("""
            SELECT * 
            FROM sra_sars_cov_2_us_east_1.annotated_variations 
            LIMIT 5
        """)
        
        then: 'Should return variation data or handle gracefully if table does not exist'
        variations != null
        
        cleanup:
        sql?.close()
    }

    @Requires({ 
        // Check if AWS credentials are available
        System.getenv('AWS_ACCESS_KEY_ID') || System.getenv('AWS_PROFILE') || 
        new File(System.getProperty('user.home') + '/.aws/credentials').exists()
    })
    def 'should query NIH SRA taxonomy analysis data'() {
        given: 'NIH SRA Athena connection parameters for taxonomy data'
        def athenaUrl = "jdbc:awsathena://AwsDataCatalog:sra_metadata_us_east_1@athena.us-east-1.amazonaws.com:443"
        def workgroupUrl = athenaUrl + ";Workgroup=primary;S3OutputLocation=s3://your-athena-results-bucket/query-results/"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Connecting to NIH SRA Athena'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.simba.athena.jdbc.Driver")
        
        then: 'Connection should be established'
        sql != null
        
        when: 'Querying taxonomy analysis data'
        def taxData = sql.rows("""
            SELECT acc, tax_id, name
            FROM sra_metadata_us_east_1.tax_analysis 
            WHERE tax_id IS NOT NULL
            LIMIT 10
        """)
        
        then: 'Should return taxonomy data'
        taxData != null
        taxData.size() > 0
        taxData.each { row ->
            assert row.acc != null
            assert row.tax_id != null
        }
        
        when: 'Querying taxonomy hierarchy'
        def taxInfo = sql.rows("""
            SELECT tax_id, parent_tax_id, rank, name_txt
            FROM sra_metadata_us_east_1.taxonomy
            WHERE rank = 'species'
            LIMIT 10
        """)
        
        then: 'Should return taxonomy hierarchy data'
        taxInfo != null
        taxInfo.size() > 0
        taxInfo.each { row ->
            assert row.tax_id != null
            assert row.rank == 'species'
        }
        
        cleanup:
        sql?.close()
    }

    def 'should handle connection failure gracefully when credentials are not available'() {
        given: 'NIH SRA Athena connection parameters with invalid/missing credentials'
        def athenaUrl = "jdbc:awsathena://AwsDataCatalog:sra_metadata_us_east_1@athena.us-east-1.amazonaws.com:443"
        def workgroupUrl = athenaUrl + ";Workgroup=primary;S3OutputLocation=s3://test-bucket/query-results/"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Attempting to connect without proper credentials'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.simba.athena.jdbc.Driver")
        sql.rows("SELECT 1")
        
        then: 'Should throw an appropriate exception'
        thrown(Exception)
    }
} 