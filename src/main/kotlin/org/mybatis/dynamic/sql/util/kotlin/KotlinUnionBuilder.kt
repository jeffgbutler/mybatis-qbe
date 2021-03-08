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
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel

@MyBatisDslMarker
class KotlinUnionBuilder(private val unionBuilder: QueryExpressionDSL<SelectModel>.UnionBuilder) {
    fun select(vararg selectList: BasicColumn, completer: SelectCompleter): Unit =
        select(selectList.toList(), completer)

    fun select(selectList: List<BasicColumn>, completer: SelectCompleter): Unit =
        completer(KotlinSelectBuilder(unionBuilder.select(selectList)))

    fun selectDistinct(vararg selectList: BasicColumn, completer: SelectCompleter): Unit =
        selectDistinct(selectList.toList(), completer)

    fun selectDistinct(selectList: List<BasicColumn>, completer: SelectCompleter): Unit =
        completer(KotlinSelectBuilder(unionBuilder.selectDistinct(selectList)))
}
