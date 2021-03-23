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
@file:Suppress("TooManyFunctions")
package org.mybatis.dynamic.sql.util.kotlin.mybatis3

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.BatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.MultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.elements.insert
import org.mybatis.dynamic.sql.util.kotlin.elements.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.elements.insertMultiple

fun count(
    mapper: (SelectStatementProvider) -> Long,
    column: BasicColumn,
    table: SqlTable,
    completer: CountCompleter
): Long =
    mapper(count(column) { completer(from(table)) })

fun countDistinct(
    mapper: (SelectStatementProvider) -> Long,
    column: BasicColumn,
    table: SqlTable,
    completer: CountCompleter
): Long =
    mapper(countDistinct(column) { completer(from(table)) })

fun countFrom(mapper: (SelectStatementProvider) -> Long, table: SqlTable, completer: CountCompleter): Long =
    mapper(countFrom(table, completer))

fun deleteFrom(mapper: (DeleteStatementProvider) -> Int, table: SqlTable, completer: DeleteCompleter): Int =
    mapper(deleteFrom(table, completer))

fun <T> insert(
    mapper: (InsertStatementProvider<T>) -> Int,
    record: T,
    table: SqlTable,
    completer: InsertCompleter<T>
): Int =
    mapper(insert(record).into(table, completer))

/**
 * This function simply inserts all rows using the supplied mapper. It is up
 * to the user to manage MyBatis3 batch processing externally.
 */
fun <T> insertBatch(
    mapper: (InsertStatementProvider<T>) -> Int,
    records: Collection<T>,
    table: SqlTable,
    completer: BatchInsertCompleter<T>
): List<Int> =
    with(insertBatch(records).into(table, completer)) {
        insertStatements().map { mapper(it) }
    }

fun insertInto(
    mapper: (GeneralInsertStatementProvider) -> Int,
    table: SqlTable,
    completer: GeneralInsertCompleter
): Int =
    mapper(insertInto(table, completer))

fun <T> insertMultiple(
    mapper: (MultiRowInsertStatementProvider<T>) -> Int,
    records: Collection<T>,
    table: SqlTable,
    completer: MultiRowInsertCompleter<T>
): Int =
    mapper(insertMultiple(records).into(table, completer))

fun insertSelect(
    mapper: (InsertSelectStatementProvider) -> Int,
    table: SqlTable,
    completer: InsertSelectCompleter
): Int =
    mapper(insertSelect(table, completer))

fun <T> selectDistinct(
    mapper: (SelectStatementProvider) -> List<T>,
    selectList: List<BasicColumn>,
    table: SqlTable,
    completer: SelectCompleter
): List<T> =
    mapper(
        selectDistinct(selectList) {
            from(table)
            completer()
        }
    )

fun <T> selectList(
    mapper: (SelectStatementProvider) -> List<T>,
    selectList: List<BasicColumn>,
    table: SqlTable,
    completer: SelectCompleter
): List<T> =
    mapper(
        select(selectList) {
            from(table)
            completer()
        }
    )

fun <T> selectOne(
    mapper: (SelectStatementProvider) -> T?,
    selectList: List<BasicColumn>,
    table: SqlTable,
    completer: SelectCompleter
): T? =
    mapper(
        select(selectList) {
            from(table)
            completer()
        }
    )

fun update(mapper: (UpdateStatementProvider) -> Int, table: SqlTable, completer: UpdateCompleter): Int =
    mapper(update(table, completer))
