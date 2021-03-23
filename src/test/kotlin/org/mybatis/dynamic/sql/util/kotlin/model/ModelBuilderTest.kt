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
package org.mybatis.dynamic.sql.util.kotlin.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo

class ModelBuilderTest {
    class Table : SqlTable("Table")

    val table = Table()
    val id = table.column<Int>("id")
    val description = table.column<String>("description")

    @Test
    fun testSelectBuilder() {
        val provider = select(id, description) {
            from(table)
            where(id, isEqualTo(3))
        }.render(RenderingStrategies.SPRING_NAMED_PARAMETER)

        assertThat(provider.selectStatement).isEqualTo("select id, description from Table where id = :p1")
    }

    @Test
    fun testSelectDistinctBuilder() {
        val provider = selectDistinct(id, description) {
            from(table)
            where(id, isEqualTo(3))
        }.render(RenderingStrategies.SPRING_NAMED_PARAMETER)

        assertThat(provider.selectStatement).isEqualTo("select distinct id, description from Table where id = :p1")
    }
}
