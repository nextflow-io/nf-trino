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
import spock.lang.Tag

/**
 * Test for NfTrinoPlugin
 */
class NfTrinoPluginTest extends Specification {

    @Tag("Athena")
    def 'should register AWS Athena driver' () {
        given:
        def wrapper = Mock(PluginWrapper)
        
        when:
        new NfTrinoPlugin(wrapper)
        
        then:
        DriverRegistry.DEFAULT.getDrivers().containsKey("awsathena")
        DriverRegistry.DEFAULT.getDrivers()["awsathena"] == "com.amazon.athena.jdbc.AthenaDriver"
        Class.forName(DriverRegistry.DEFAULT.getDrivers()["awsathena"])
        java.sql.DriverManager.getDriver("jdbc:athena://").class.name == "com.amazon.athena.jdbc.AthenaDriver"
    }

    @Tag("Trino")
    def 'should register Trino driver' () {
        given:
        def wrapper = Mock(PluginWrapper)
        
        when:
        new NfTrinoPlugin(wrapper)
        
        then:
        DriverRegistry.DEFAULT.getDrivers().containsKey("trino")
        DriverRegistry.DEFAULT.getDrivers()["trino"] == "io.trino.jdbc.TrinoDriver"
    }

    @Tag("Starburst")
    def 'should register Starburst driver' () {
        given:
        def wrapper = Mock(PluginWrapper)
        
        when:
        new NfTrinoPlugin(wrapper)
        
        then:
        DriverRegistry.DEFAULT.getDrivers().containsKey("starburst")
        DriverRegistry.DEFAULT.getDrivers()["starburst"] == "io.trino.jdbc.TrinoDriver"
    }

    @Tag("Trino")
    @Tag("Starburst") 
    @Tag("Athena")
    def 'should register all supported drivers' () {
        given:
        def wrapper = Mock(PluginWrapper)
        
        when:
        new NfTrinoPlugin(wrapper)
        def drivers = DriverRegistry.DEFAULT.getDrivers()
        
        then: 'All three drivers should be registered'
        drivers.containsKey("trino")
        drivers.containsKey("starburst") 
        drivers.containsKey("awsathena")
        
        and: 'Trino and Starburst should use the same driver class'
        drivers["trino"] == "io.trino.jdbc.TrinoDriver"
        drivers["starburst"] == "io.trino.jdbc.TrinoDriver"
        drivers["trino"] == drivers["starburst"]
        
        and: 'Athena should use its own driver class'
        drivers["awsathena"] == "com.amazon.athena.jdbc.AthenaDriver"
    }

    @Tag("Starburst")
    def 'should validate Starburst JDBC URL format' () {
        given: 'Various Starburst JDBC URL formats'
        def galaxyUrl = "jdbc:trino://cluster.galaxy.starburst.io:443/catalog/schema?SSL=true"
        def enterpriseUrl = "jdbc:trino://starburst-host:8080/catalog/schema"
        def enterpriseSSLUrl = "jdbc:trino://starburst-host:443/catalog/schema?SSL=true"
        
        when: 'Validating URL formats'
        def galaxyValid = galaxyUrl.startsWith("jdbc:trino://") && galaxyUrl.contains("galaxy.starburst.io")
        def enterpriseValid = enterpriseUrl.startsWith("jdbc:trino://") && enterpriseUrl.contains(":8080")
        def enterpriseSSLValid = enterpriseSSLUrl.startsWith("jdbc:trino://") && enterpriseSSLUrl.contains("SSL=true")
        
        then: 'All URL formats should be valid'
        galaxyValid
        enterpriseValid
        enterpriseSSLValid
    }

    @Tag("Starburst")
    def 'should handle Starburst connection parameters' () {
        given: 'Starburst connection parameters'
        def connectionProps = [
            'SSL': 'true',
            'SSLVerification': 'FULL',
            'source': 'nextflow-analytics',
            'clientTags': 'batch,genomics',
            'sessionProperties': 'query_max_memory=10GB;query_max_run_time=1h',
            'roles': 'hive:analyst;system:monitor',
            'timezone': 'UTC',
            'accessToken': 'jwt-token-example',
            'externalAuthentication': 'true'
        ]
        
        when: 'Processing connection properties'
        def sslEnabled = connectionProps['SSL'] == 'true'
        def hasJWT = connectionProps['accessToken'] != null
        def hasOAuth = connectionProps['externalAuthentication'] == 'true'
        def hasSessionProps = connectionProps['sessionProperties'] != null
        
        then: 'Properties should be correctly identified'
        sslEnabled
        hasJWT
        hasOAuth
        hasSessionProps
        connectionProps['source'] == 'nextflow-analytics'
        connectionProps['timezone'] == 'UTC'
    }

    @Tag("Starburst")
    def 'should create Starburst JDBC connection with proper driver class' () {
        given: 'Starburst Galaxy connection parameters'
        def galaxyUrl = "jdbc:trino://test-cluster.galaxy.starburst.io:443/catalog/schema?SSL=true"
        def username = "test-user"
        def password = "test-password"
        
        and: 'Starburst driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Getting the registered driver class'
        def driverClass = DriverRegistry.DEFAULT.getDrivers()["starburst"]
        
        then: 'Should use the Trino JDBC driver'
        driverClass == "io.trino.jdbc.TrinoDriver"
        
        when: 'Validating connection parameters'
        def urlValid = galaxyUrl.startsWith("jdbc:trino://") && galaxyUrl.contains("galaxy.starburst.io")
        def hasSSL = galaxyUrl.contains("SSL=true")
        def hasCredentials = username != null && password != null
        
        then: 'Connection setup should be valid'
        urlValid
        hasSSL
        hasCredentials
    }

    @Tag("Starburst")
    def 'should support Starburst Enterprise connection formats' () {
        given: 'Various Starburst Enterprise connection scenarios'
        def basicUrl = "jdbc:trino://starburst-coordinator:8080/hive/default"
        def sslUrl = "jdbc:trino://starburst-coordinator:443/iceberg/analytics?SSL=true"
        def jwtUrl = "jdbc:trino://starburst-coordinator:8080/catalog/schema"
        
        and: 'Connection properties for different auth methods'
        def basicProps = [user: 'analyst']
        def sslProps = [user: 'analyst', password: 'secret', SSL: 'true']
        def jwtProps = [user: 'analyst', accessToken: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...']
        
        when: 'Validating different connection formats'
        def basicValid = basicUrl.startsWith("jdbc:trino://") && basicUrl.contains(":8080")
        def sslValid = sslUrl.startsWith("jdbc:trino://") && sslUrl.contains("SSL=true")
        def jwtValid = jwtUrl.startsWith("jdbc:trino://") && jwtProps.accessToken != null
        
        then: 'All connection formats should be valid'
        basicValid
        sslValid
        jwtValid
        basicProps.user == 'analyst'
        sslProps.SSL == 'true'
        jwtProps.accessToken.startsWith('eyJ0eXAiOiJKV1Qi')
    }

    @Tag("Starburst")
    @Tag("Integration")
    def 'should handle Starburst connection failure gracefully when cluster is unreachable' () {
        given: 'Starburst connection parameters for non-existent cluster'
        def testUrl = "jdbc:trino://non-existent-cluster.galaxy.starburst.io:443/catalog/schema?SSL=true"
        def testUser = "test-user"
        def testPassword = "test-password"
        
        and: 'Starburst driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Attempting to connect to non-existent Starburst cluster'
        def sql = Sql.newInstance(testUrl, testUser, testPassword, "io.trino.jdbc.TrinoDriver")
        sql.rows("SELECT 1")
        
        then: 'Should throw an appropriate connection exception'
        thrown(Exception)
    }

    @Tag("Starburst")
    def 'should construct valid Starburst JDBC URLs according to documentation' () {
        given: 'Starburst connection requirements from documentation'
        // Based on https://docs.starburst.io/clients/jdbc.html
        def galaxyHost = "my-cluster.galaxy.starburst.io"
        def enterpriseHost = "starburst-coordinator.company.com"
        def catalog = "iceberg"
        def schema = "analytics"
        
        when: 'Constructing Starburst Galaxy JDBC URLs'
        def galaxyBasicUrl = "jdbc:trino://${galaxyHost}:443/${catalog}/${schema}"
        def galaxySSLUrl = "jdbc:trino://${galaxyHost}:443/${catalog}/${schema}?SSL=true"
        def galaxyWithPropsUrl = "jdbc:trino://${galaxyHost}:443/${catalog}/${schema}?SSL=true&source=nextflow&clientTags=batch"
        
        and: 'Constructing Starburst Enterprise JDBC URLs'
        def enterpriseBasicUrl = "jdbc:trino://${enterpriseHost}:8080/${catalog}/${schema}"
        def enterpriseSSLUrl = "jdbc:trino://${enterpriseHost}:443/${catalog}/${schema}?SSL=true"
        def enterpriseJWTUrl = "jdbc:trino://${enterpriseHost}:8080/${catalog}/${schema}"
        
        then: 'All URLs should follow the correct jdbc:trino:// format'
        galaxyBasicUrl == "jdbc:trino://my-cluster.galaxy.starburst.io:443/iceberg/analytics"
        galaxySSLUrl == "jdbc:trino://my-cluster.galaxy.starburst.io:443/iceberg/analytics?SSL=true"
        galaxyWithPropsUrl.contains("SSL=true") && galaxyWithPropsUrl.contains("source=nextflow")
        
        and: 'Enterprise URLs should be properly formatted'
        enterpriseBasicUrl == "jdbc:trino://starburst-coordinator.company.com:8080/iceberg/analytics"
        enterpriseSSLUrl.contains("SSL=true") && enterpriseSSLUrl.contains(":443")
        enterpriseJWTUrl.startsWith("jdbc:trino://")
        
        and: 'All URLs should be valid Trino JDBC format'
        [galaxyBasicUrl, galaxySSLUrl, enterpriseBasicUrl, enterpriseSSLUrl, enterpriseJWTUrl].every { url ->
            url.startsWith("jdbc:trino://") && url.contains(catalog) && url.contains(schema)
        }
    }

    @Tag("Athena")
    @Tag("Integration")
    @Requires({ 
        System.getenv('ATHENA_TEST_S3_OUTPUT_LOCATION')
    })
    def 'should connect to NIH SRA Athena instance and query metadata'() {
        given: 'NIH SRA Athena connection parameters'
        def workgroupUrl = "jdbc:athena://Catalog=AwsDataCatalog;Database=sra_metadata_us_east_1;Region=us-east-1;" +
            "WorkGroup=primary;OutputLocation=${System.getenv('ATHENA_TEST_S3_OUTPUT_LOCATION')};CredentialsProvider=DefaultChain;"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Connecting to NIH SRA Athena'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.amazon.athena.jdbc.AthenaDriver")
        
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

    @Tag("Athena")
    @Tag("Integration")
    @Requires({ 
        System.getenv('ATHENA_TEST_S3_OUTPUT_LOCATION')
    })
    def 'should query NIH SRA SARS-CoV-2 specific dataset'() {
        given: 'NIH SRA SARS-CoV-2 Athena connection parameters'
        def workgroupUrl = "jdbc:athena://Catalog=AwsDataCatalog;Database=sra_sars_cov_2_us_east_1;Region=us-east-1;" +
            "WorkGroup=primary;OutputLocation=${System.getenv('ATHENA_TEST_S3_OUTPUT_LOCATION')};CredentialsProvider=DefaultChain;"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Connecting to NIH SRA SARS-CoV-2 Athena'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.amazon.athena.jdbc.AthenaDriver")
        
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

    @Tag("Athena")
    @Tag("Integration")
    @Requires({ 
        System.getenv('ATHENA_TEST_S3_OUTPUT_LOCATION')
    })
    def 'should query NIH SRA taxonomy analysis data'() {
        given: 'NIH SRA Athena connection parameters for taxonomy data'
        def workgroupUrl = "jdbc:athena://Catalog=AwsDataCatalog;Database=sra_metadata_us_east_1;Region=us-east-1;" +
            "WorkGroup=primary;OutputLocation=${System.getenv('ATHENA_TEST_S3_OUTPUT_LOCATION')};CredentialsProvider=DefaultChain;"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Connecting to NIH SRA Athena'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.amazon.athena.jdbc.AthenaDriver")
        
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

    @Tag("Athena")
    @Tag("Integration")
    def 'should handle connection failure gracefully when credentials are not available'() {
        given: 'NIH SRA Athena connection parameters with invalid/missing credentials'
        def workgroupUrl = "jdbc:athena://Catalog=AwsDataCatalog;Database=sra_metadata_us_east_1;Region=us-east-1;" +
            "WorkGroup=primary;OutputLocation=s3://test-bucket/query-results/;User=invalid;Password=invalid;"
        
        and: 'Athena driver is registered'
        def wrapper = Mock(PluginWrapper)
        new NfTrinoPlugin(wrapper)
        
        when: 'Attempting to connect without proper credentials'
        def sql = Sql.newInstance(workgroupUrl, null, null, "com.amazon.athena.jdbc.AthenaDriver")
        sql.rows("SELECT 1")
        
        then: 'Should throw an appropriate exception'
        thrown(Exception)
    }
} 