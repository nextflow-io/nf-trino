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
import org.testcontainers.containers.TrinoContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

/**
 * Trino integration tests using Testcontainers
 * 
 * These tests verify that the core Trino JDBC driver works correctly 
 * with a real Trino instance running in a container.
 * 
 * NOTE: This test class focuses ONLY on testing the "trino" driver
 * and does NOT test Athena or Starburst functionality - those are
 * tested separately in NfTrinoPluginTest.groovy
 */
@Testcontainers
class TrinoContainerTest extends Specification {

    @Shared
    TrinoContainer trino = new TrinoContainer("trinodb/trino:451")
            .withUsername("test")

    def setupSpec() {
        // Start the Trino container
        trino.start()
        
        // Wait for Trino to be fully ready by checking if we can connect and run queries
        def maxRetries = 60  // Increased retries
        def retryDelay = 2000 // 2 seconds
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                def sql = Sql.newInstance(trino.getJdbcUrl(), trino.getUsername(), trino.getPassword(), "io.trino.jdbc.TrinoDriver")
                
                // Test basic connectivity
                def result = sql.rows('SELECT 1')
                if (result.size() == 1) {
                    // Test that we can actually create tables (more comprehensive readiness check)
                    sql.execute('CREATE TABLE IF NOT EXISTS memory.default.readiness_test (id bigint)')
                    sql.execute('DROP TABLE IF EXISTS memory.default.readiness_test')
                    sql.close()
                    println "Trino container is ready after ${i + 1} attempts"
                    break
                }
                sql.close()
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    throw new RuntimeException("Trino container failed to become ready after ${maxRetries} attempts", e)
                }
                println "Waiting for Trino to be ready... attempt ${i + 1}/${maxRetries}: ${e.message}"
                Thread.sleep(retryDelay)
            }
        }
    }
    
    def cleanupSpec() {
        // Stop the container
        trino.stop()
    }

    @Timeout(30)
    def 'should connect to Trino container and execute basic queries'() {
        given:
        def sql = Sql.newInstance(trino.getJdbcUrl(), trino.getUsername(), trino.getPassword(), "io.trino.jdbc.TrinoDriver")

        when: 'Creating a test table'
        sql.execute('CREATE TABLE memory.default.test_table (id bigint, name varchar)')
        sql.execute("INSERT INTO memory.default.test_table VALUES (1, 'hello'), (2, 'world')")

        then: 'Table should be created successfully'
        def tables = sql.rows("SHOW TABLES FROM memory.default")
        tables.find { it.values().contains('test_table') } != null

        when: 'Querying the test table'
        def results = sql.rows('SELECT * FROM memory.default.test_table ORDER BY id')

        then: 'Should return inserted data'
        results.size() == 2
        results[0].id == 1
        results[0].name == 'hello'
        results[1].id == 2
        results[1].name == 'world'

        cleanup:
        sql?.execute('DROP TABLE IF EXISTS memory.default.test_table')
        sql?.close()
    }

    @Timeout(30)
    def 'should create and query tables in Trino container'() {
        given:
        def sql = Sql.newInstance(trino.getJdbcUrl(), trino.getUsername(), trino.getPassword(), "io.trino.jdbc.TrinoDriver")
        
        when: 'Create test data'
        sql.execute('CREATE TABLE memory.default.sample_data (id bigint, alpha varchar, omega bigint)')
        sql.execute("INSERT INTO memory.default.sample_data VALUES (1, 'hola', 10), (2, 'ciao', 20), (3, 'hello', 30)")
        
        and: 'Query the data'
        def results = sql.rows("SELECT * FROM memory.default.sample_data ORDER BY id")

        then: 'Should return query results'
        results.size() == 3
        results[0].id == 1L
        results[0].alpha == 'hola'
        results[0].omega == 10L
        results[1].id == 2L
        results[1].alpha == 'ciao'
        results[1].omega == 20L
        results[2].id == 3L
        results[2].alpha == 'hello'
        results[2].omega == 30L

        cleanup:
        sql?.execute('DROP TABLE IF EXISTS memory.default.sample_data')
        sql?.close()
    }

    @Timeout(30)
    def 'should handle Trino-specific data types and functions'() {
        given:
        def sql = Sql.newInstance(trino.getJdbcUrl(), trino.getUsername(), trino.getPassword(), "io.trino.jdbc.TrinoDriver")

        when: 'Testing Trino-specific functions'
        def results = sql.rows("""
            SELECT 
                current_timestamp as ts,
                array[1, 2, 3] as arr,
                map(array['key1', 'key2'], array['value1', 'value2']) as mp,
                json_parse('{"name": "test"}') as json_data
        """)

        then: 'Should handle Trino data types'
        results.size() == 1
        results[0].ts != null
        results[0].arr != null
        results[0].mp != null
        results[0].json_data != null

        cleanup:
        sql?.close()
    }

    @Timeout(30) 
    def 'should work with multiple catalogs and schemas in Trino'() {
        given:
        def sql = Sql.newInstance(trino.getJdbcUrl(), trino.getUsername(), trino.getPassword(), "io.trino.jdbc.TrinoDriver")

        when: 'Listing available catalogs'
        def catalogs = sql.rows('SHOW CATALOGS')
        
        then: 'Should have at least the memory catalog'
        catalogs.find { it.values().contains('memory') } != null

        when: 'Listing schemas in memory catalog'
        def schemas = sql.rows('SHOW SCHEMAS FROM memory')
        
        then: 'Should have default schema'
        schemas.find { it.values().contains('default') } != null

        when: 'Creating table in memory catalog'
        sql.execute('CREATE TABLE memory.default.multi_catalog_test (id bigint, data varchar)')
        sql.execute("INSERT INTO memory.default.multi_catalog_test VALUES (1, 'catalog_test')")
        
        def data = sql.rows('SELECT * FROM memory.default.multi_catalog_test')
        
        then: 'Should access data from specific catalog.schema.table'
        data.size() == 1
        data[0].id == 1
        data[0].data == 'catalog_test'

        cleanup:
        sql?.execute('DROP TABLE IF EXISTS memory.default.multi_catalog_test')
        sql?.close()
    }

    @Timeout(30)
    def 'should verify Trino driver registration and connectivity'() {
        given: 'Initialize the NfTrinoPlugin to register drivers'
        def wrapper = Mock(org.pf4j.PluginWrapper)
        new NfTrinoPlugin(wrapper)

        when: 'Check if Trino driver is registered (not Athena or Starburst)'
        def trinoDriverClass = nextflow.sql.config.DriverRegistry.DEFAULT.getDrivers()["trino"]

        then: 'Should have the correct Trino driver class'
        trinoDriverClass == "io.trino.jdbc.TrinoDriver"

        when: 'Connect using the registered Trino driver with container'
        def sql = Sql.newInstance(trino.getJdbcUrl(), trino.getUsername(), trino.getPassword(), trinoDriverClass)
        def result = sql.rows('SELECT 1 as test_value')

        then: 'Should successfully connect and query using Trino driver'
        result.size() == 1
        result[0].test_value == 1

        cleanup:
        sql?.close()
    }
} 