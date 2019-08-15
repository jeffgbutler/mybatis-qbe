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
package examples.kotlin

import examples.kotlin.PersonDynamicSqlSupport.Person
import examples.kotlin.PersonDynamicSqlSupport.Person.addressId
import examples.kotlin.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.PersonDynamicSqlSupport.Person.id
import examples.kotlin.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.PersonDynamicSqlSupport.Person.occupation
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlBuilder.count
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.*

fun PersonMapper.count(helper: CountHelper) =
        helper(selectWithKotlinMapper(this::count, count())
                .from(Person))
                .build()
                .execute()

fun PersonMapper.delete(helper: DeleteHelper) =
        helper(deleteWithKotlinMapper(this::delete, Person))
                .build()
                .execute()

fun PersonMapper.deleteByPrimaryKey(id_: Int) =
        delete {
            where(id, isEqualTo(id_))
        }

fun PersonMapper.insert(record: PersonRecord) =
        insert(SqlBuilder.insert(record)
                .into(Person)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employed")
                .map(occupation).toProperty("occupation")
                .map(addressId).toProperty("addressId")
                .build()
                .render(RenderingStrategy.MYBATIS3))

fun PersonMapper.insertMultiple(vararg records: PersonRecord) =
        insertMultiple(SqlBuilder.insertMultiple(*records)
                .into(Person)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employed")
                .map(occupation).toProperty("occupation")
                .map(addressId).toProperty("addressId")
                .build()
                .render(RenderingStrategy.MYBATIS3))

private fun selectList() =
        arrayOf(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)

fun PersonMapper.selectOne(helper: SelectOneHelper<PersonRecord>) =
        helper(selectWithKotlinMapper(this::selectOne, *selectList())
                .from(Person))
                .build()
                .execute()

fun PersonMapper.select(helper: SelectListHelper<PersonRecord>) =
        helper(selectWithKotlinMapper(this::selectMany, *selectList())
                .from(Person))
                .build()
                .execute()

fun PersonMapper.selectDistinct(helper: SelectListHelper<PersonRecord>) =
        helper(selectDistinctWithKotlinMapper(this::selectMany, *selectList())
                .from(Person))
                .build()
                .execute()

fun PersonMapper.selectByPrimaryKey(id_: Int) =
        selectOne {
            where(id, isEqualTo(id_))
        }

fun PersonMapper.update(helper: UpdateHelper) =
        helper(updateWithKotlinMapper(this::update, Person)).build().execute()

fun PersonMapper.updateByPrimaryKey(record: PersonRecord) =
        update {
            set(firstName).equalTo(record::firstName)
            set(lastName).equalTo(record::lastName)
            set(birthDate).equalTo(record::birthDate)
            set(employed).equalTo(record::employed)
            set(occupation).equalTo(record::occupation)
            set(addressId).equalTo(record::addressId)
            where(id, isEqualTo(record::id))
        }

fun PersonMapper.updateByPrimaryKeySelective(record: PersonRecord) =
        update {
            set(firstName).equalToWhenPresent(record::firstName)
            set(lastName).equalToWhenPresent(record::lastName)
            set(birthDate).equalToWhenPresent(record::birthDate)
            set(employed).equalToWhenPresent(record::employed)
            set(occupation).equalToWhenPresent(record::occupation)
            set(addressId).equalToWhenPresent(record::addressId)
            where(id, isEqualTo(record::id))
        }

fun UpdateDSL<UpdateModelAdapter>.setAll(record: PersonRecord) =
        apply {
            set(id).equalTo(record::id)
            set(firstName).equalTo(record::firstName)
            set(lastName).equalTo(record::lastName)
            set(birthDate).equalTo(record::birthDate)
            set(employed).equalTo(record::employed)
            set(occupation).equalTo(record::occupation)
            set(addressId).equalTo(record::addressId)
        }

fun UpdateDSL<UpdateModelAdapter>.setSelective(record: PersonRecord) =
        apply {
            set(id).equalToWhenPresent(record::id)
            set(firstName).equalToWhenPresent(record::firstName)
            set(lastName).equalToWhenPresent(record::lastName)
            set(birthDate).equalToWhenPresent(record::birthDate)
            set(employed).equalToWhenPresent(record::employed)
            set(occupation).equalToWhenPresent(record::occupation)
            set(addressId).equalToWhenPresent(record::addressId)
        }