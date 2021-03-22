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
package examples.kotlin.mybatis3.custom.render

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column
import java.sql.JDBCType

object KJsonTestDynamicSqlSupport {
    val jsonTest = JsonTest()
    val id = jsonTest.id
    val description = jsonTest.description
    val info = jsonTest.info

    class JsonTest : SqlTable("JsonTest") {
        val id = column<Int>(name = "id", jdbcType = JDBCType.INTEGER)
        val description = column<String>(name = "description", jdbcType= JDBCType.VARCHAR)
        val info = column<String>(
            name = "info",
            JDBCType.VARCHAR,
            renderingStrategy = KJsonRenderingStrategy()
        )
    }
}
