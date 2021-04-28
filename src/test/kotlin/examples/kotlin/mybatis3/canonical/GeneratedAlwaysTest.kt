/*
 *    Copyright 2016-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.kotlin.mybatis3.canonical

import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.generatedAlways
import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.lastName
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertInto
import java.io.InputStreamReader
import java.sql.DriverManager

class GeneratedAlwaysTest {
    private fun newSession(executorType: ExecutorType = ExecutorType.REUSE): SqlSession {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/spring/CreateGeneratedAlwaysDB.sql")
        DriverManager.getConnection(JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script))
        }

        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.addMapper(GeneratedAlwaysMapper::class.java)
        return SqlSessionFactoryBuilder().build(config).openSession(executorType)
    }

    @Test
    fun testInsertSingleRecord() {
        newSession().use { session ->
            val mapper = session.getMapper(GeneratedAlwaysMapper::class.java)

            val record = GeneratedAlwaysRecord(
                firstName = "Fred",
                lastName = "Flintstone"
            )

            val rows = mapper.insert(record)

            assertThat(rows).isEqualTo(1)
            assertThat(record.id).isEqualTo(22)
            assertThat(record.fullName).isEqualTo("Fred Flintstone")
        }
    }

    @Test
    fun testInsertMultiple() {
        newSession().use { session ->
            val mapper = session.getMapper(GeneratedAlwaysMapper::class.java)

            val record1 = GeneratedAlwaysRecord(
                firstName = "Fred",
                lastName = "Flintstone"
            )

            val record2 = GeneratedAlwaysRecord(
                firstName = "Barney",
                lastName = "Rubble"
            )

            val rows = mapper.insertMultiple(listOf(record1, record2))

            assertThat(rows).isEqualTo(2)
            assertThat(record1.id).isEqualTo(22)
            assertThat(record1.fullName).isEqualTo("Fred Flintstone")
            assertThat(record2.id).isEqualTo(23)
            assertThat(record2.fullName).isEqualTo("Barney Rubble")
        }
    }

    @Test
    fun testInsertBatch() {
        newSession(ExecutorType.BATCH).use { session ->
            val mapper = session.getMapper(GeneratedAlwaysMapper::class.java)

            val record1 = GeneratedAlwaysRecord(
                firstName = "Fred",
                lastName = "Flintstone"
            )

            val record2 = GeneratedAlwaysRecord(
                firstName = "Barney",
                lastName = "Rubble"
            )

            mapper.insertBatch(listOf(record1, record2))

            val batchResults = mapper.flush()

            assertThat(batchResults).hasSize(1)
            assertThat(batchResults[0].updateCounts).hasSize(2)
            assertThat(batchResults[0].updateCounts[0]).isEqualTo(1)
            assertThat(batchResults[0].updateCounts[1]).isEqualTo(1)

            assertThat(record1.id).isEqualTo(22)
            assertThat(record1.fullName).isEqualTo("Fred Flintstone")
            assertThat(record2.id).isEqualTo(23)
            assertThat(record2.fullName).isEqualTo("Barney Rubble")
        }
    }

    @Test
    fun testGeneralInsert() {
        newSession().use { session ->
            val mapper = session.getMapper(GeneratedAlwaysMapper::class.java)

            val insertStatement = insertInto(generatedAlways) {
                set(firstName).toValue("Fred")
                set(lastName).toValue("Flintstone")
            }

            val rows = mapper.generalInsert(insertStatement)
            val id = insertStatement.parameters["id"] as Int
            val fullName = insertStatement.parameters["fullName"] as String

            assertThat(rows).isEqualTo(1)
            assertThat(id).isEqualTo(22)
            assertThat(fullName).isEqualTo("Fred Flintstone")
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
