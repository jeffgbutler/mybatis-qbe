/**
 *    Copyright 2016-2019 the original author or authors.
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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.delete.DeleteDSL
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.delete.whereBuilder
import org.mybatis.dynamic.sql.util.Buildable

fun <T> DeleteDSL<DeleteModel>.where(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                     collect: CriteriaCollector.() -> CriteriaCollector): DeleteDSL<DeleteModel> {
    val collector = CriteriaCollector()
    collect(collector)
    this.where(column, condition, *collector.criteria())
    return this
}

fun <T> DeleteDSL<DeleteModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>): DeleteDSL<DeleteModel> {
    whereBuilder()?.and(column, condition)
    return this
}

fun <T> DeleteDSL<DeleteModel>.and(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                   collect: CriteriaCollector.() -> CriteriaCollector): DeleteDSL<DeleteModel> {
    val collector = CriteriaCollector()
    collect(collector)
    whereBuilder()?.and(column, condition, *collector.criteria())
    return this
}

fun <T> DeleteDSL<DeleteModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>): DeleteDSL<DeleteModel> {
    whereBuilder()?.or(column, condition)
    return this
}

fun <T> DeleteDSL<DeleteModel>.or(column: BindableColumn<T>, condition: VisitableCondition<T>,
                                  collect: CriteriaCollector.() -> CriteriaCollector): DeleteDSL<DeleteModel> {
    val collector = CriteriaCollector()
    collect(collector)
    whereBuilder()?.or(column, condition, *collector.criteria())
    return this
}

fun DeleteDSL<DeleteModel>.allRows() = this as Buildable<DeleteModel>