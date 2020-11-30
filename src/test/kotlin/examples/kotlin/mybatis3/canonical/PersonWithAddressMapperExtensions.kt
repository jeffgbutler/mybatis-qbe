/*
 *    Copyright 2016-2020 the original author or authors.
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

import examples.kotlin.mybatis3.canonical.AddressDynamicSqlSupport.Address
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.occupation
import org.mybatis.dynamic.sql.SqlBuilder.equalTo
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.SqlBuilder.select
import org.mybatis.dynamic.sql.SqlBuilder.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.KotlinSelectBuilder
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectList
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectOne

fun PersonWithAddressMapper.selectOne(completer: SelectCompleter): PersonWithAddress? {
    val start = KotlinSelectBuilder(select(id.`as`("A_ID"), firstName, lastName, birthDate,
        employed, occupation, Address.id, Address.streetAddress, Address.city, Address.state)).apply {
        from(Person)
        fullJoin(Address) {
            on(Person.addressId, equalTo(Address.id))
        }
    }

    return selectOne(this::selectOne, start, completer)
}

fun PersonWithAddressMapper.select(completer: SelectCompleter): List<PersonWithAddress> {
    val start = KotlinSelectBuilder(select(id.`as`("A_ID"), firstName, lastName, birthDate,
        employed, occupation, Address.id, Address.streetAddress, Address.city, Address.state)).apply {
        from(Person, "p")
        fullJoin(Address) {
            on(Person.addressId, equalTo(Address.id))
        }
    }

    return selectList(this::selectMany, start, completer)
}

fun PersonWithAddressMapper.selectDistinct(completer: SelectCompleter): List<PersonWithAddress> {
    val start = KotlinSelectBuilder(selectDistinct(id.`as`("A_ID"), firstName, lastName,
        birthDate, employed, occupation, Address.id, Address.streetAddress, Address.city, Address.state)).apply {
        from(Person, "p")
        fullJoin(Address) {
            on(Person.addressId, equalTo(Address.id))
        }
    }

    return selectList(this::selectMany, start, completer)
}

fun PersonWithAddressMapper.selectByPrimaryKey(id_: Int) =
    selectOne {
        where(id, isEqualTo(id_))
    }
