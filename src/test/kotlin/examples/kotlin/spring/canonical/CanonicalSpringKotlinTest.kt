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
package examples.kotlin.spring.canonical

import examples.kotlin.spring.canonical.AddressDynamicSqlSupport.Address
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.addressId
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.occupation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.SqlBuilder.*
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.spring.*
import org.mybatis.dynamic.sql.util.kotlin.spring.from
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import java.sql.ResultSet
import java.util.*

fun NamedParameterJdbcTemplate.count(selectStatement: SelectStatementProvider) =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, Long::class.java)
        ?: 0

fun NamedParameterJdbcTemplate.delete(deleteStatement: DeleteStatementProvider) =
    update(deleteStatement.deleteStatement, deleteStatement.parameters)

fun <T> NamedParameterJdbcTemplate.insert(insertStatement: InsertStatementProvider<T>) =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement.record))

fun <T> NamedParameterJdbcTemplate.selectMany(selectStatement: SelectStatementProvider, rowMapper: (rs: ResultSet, rowNum: Int) -> T): List<T> =
    query(selectStatement.selectStatement, selectStatement.parameters, rowMapper)

fun <T> NamedParameterJdbcTemplate.selectOne(selectStatement: SelectStatementProvider, rowMapper: (rs: ResultSet, rowNum: Int) -> T): T =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, rowMapper)!!

fun NamedParameterJdbcTemplate.update(updateStatement: UpdateStatementProvider) =
    update(updateStatement.updateStatement, updateStatement.parameters)

class CanonicalSpringKotlinTest {
    private lateinit var db: EmbeddedDatabase
    private lateinit var template: NamedParameterJdbcTemplate

    @AfterEach
    fun teardown() {
        db.shutdown()
    }

    @BeforeEach
    fun setup() {
        db = EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .generateUniqueName(true)
            .addScript("classpath:/examples/kotlin/spring/CreateSimpleDB.sql")
            .build()
        template = NamedParameterJdbcTemplate(db)
    }

    @Test
    fun testRawCount() {
        val countStatement = count(Person) {
            where(id, isLessThan(4))
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(*) from Person" +
            " where id < :p1")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawCountAllRows() {
        val countStatement = count(Person) {
            allRows()
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(*) from Person")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testRawDelete1() {
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person" +
            " where id < :p1")

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawDelete2() {
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            and(occupation, isNotNull())
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person" +
            " where id < :p1 and occupation is not null")

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testRawDelete3() {

        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            or(occupation, isNotNull())
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person" +
            " where id < :p1 or occupation is not null")

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testRawDelete4() {

        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4)) {
                or(occupation, isNotNull())
            }
            and(employed, isEqualTo("Yes"))
        }

        val expected = "delete from Person" +
            " where (id < :p1 or occupation is not null)" +
            " and employed = :p2"

        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testRawDelete5() {
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            or(occupation, isNotNull()) {
                and(employed, isEqualTo("Yes"))
            }
        }

        val expected = "delete from Person" +
            " where id < :p1 or (occupation is not null" +
            " and employed =" +
            " :p2)"

        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testRawDelete6() {
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            and(occupation, isNotNull()) {
                and(employed, isEqualTo("Yes"))
            }
        }

        val expected = "delete from Person where id < :p1" +
            " and (occupation is not null and" +
            " employed = :p2)"

        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testInsert() {

        val record = PersonRecord(100, "Joe", "Jones", Date(), "Yes", "Developer", 1)

        val insertStatement = insert(record, Person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastName")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employed")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        val expected = "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
            " values" +
            " (:id, :firstName," +
            " :lastName," +
            " :birthDate, :employed," +
            " :occupation, :addressId)"

        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.insert(insertStatement)

        assertThat(rows).isEqualTo(1)
    }

//    @Test
//    fun testInsertMultiple() {
//        val record1 = PersonRecord(100, "Joe", "Jones", Date(), "Yes", "Developer", 1)
//        val record2 = PersonRecord(101, "Sarah", "Smith", Date(), "Yes", "Architect", 2)
//
//        val insertStatement = insertMultiple(listOf(record1, record2), Person) {
//            map(id).toProperty("id")
//            map(firstName).toProperty("firstName")
//            map(lastName).toProperty("lastName")
//            map(birthDate).toProperty("birthDate")
//            map(employed).toProperty("employed")
//            map(occupation).toProperty("occupation")
//            map(addressId).toProperty("addressId")
//        }
//
//        val expected = "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
//            " values" +
//            " (#{records[0].id,jdbcType=INTEGER}," +
//            " #{records[0].firstName,jdbcType=VARCHAR}," +
//            " #{records[0].lastName,jdbcType=VARCHAR,typeHandler=examples.kotlin.mybatis3.canonical.LastNameTypeHandler}," +
//            " #{records[0].birthDate,jdbcType=DATE}," +
//            " #{records[0].employed,jdbcType=VARCHAR,typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler}," +
//            " #{records[0].occupation,jdbcType=VARCHAR}," +
//            " #{records[0].addressId,jdbcType=INTEGER})" +
//            ", (#{records[1].id,jdbcType=INTEGER}," +
//            " #{records[1].firstName,jdbcType=VARCHAR}," +
//            " #{records[1].lastName,jdbcType=VARCHAR,typeHandler=examples.kotlin.mybatis3.canonical.LastNameTypeHandler}," +
//            " #{records[1].birthDate,jdbcType=DATE}," +
//            " #{records[1].employed,jdbcType=VARCHAR,typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler}," +
//            " #{records[1].occupation,jdbcType=VARCHAR}," +
//            " #{records[1].addressId,jdbcType=INTEGER})"
//
//        assertThat(insertStatement.insertStatement).isEqualTo(expected)
//
//        val b = BeanPropertySqlParameterSource(insertStatement.records)
//        val names = b.parameterNames
//
//        val rows = template.update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement.records))
//
//        assertThat(rows).isEqualTo(2)
//    }

    @Test
    fun testRawSelect() {
        val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId).from(Person) {
            where(id, isLessThan(4)) {
                and(occupation, isNotNull())
            }
            and(occupation, isNotNull())
            orderBy(id)
            limit(3)
        }

        val rows = template.selectMany(selectStatement) { rs, _ ->
            val record = PersonRecord()
            record.id = rs.getInt(1)
            record.firstName = rs.getString(2)
            record.lastName = rs.getString(3)
            record.birthDate = rs.getTimestamp(4)
            record.employed = rs.getString(5)
            record.occupation = rs.getString(6)
            record.addressId = rs.getInt(7)
            record
        }

        assertThat(rows.size).isEqualTo(2)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isEqualTo("Yes")
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectByPrimaryKey() {
        val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId).from(Person) {
            where(id, isEqualTo(1))
        }

        val record = template.selectOne(selectStatement) { rs, _ ->
            val record = PersonRecord()
            record.id = rs.getInt(1)
            record.firstName = rs.getString(2)
            record.lastName = rs.getString(3)
            record.birthDate = rs.getTimestamp(4)
            record.employed = rs.getString(5)
            record.occupation = rs.getString(6)
            record.addressId = rs.getInt(7)
            record
        }

        with(record) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isEqualTo("Yes")
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithJoin() {
        val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            Address.id, Address.streetAddress, Address.city, Address.state)
            .from(Person, "p") {
                join(Address, "a") {
                    on(addressId, equalTo(Address.id))
                }
                where(id, isLessThan(4))
                orderBy(id)
                limit(3)
            }

        val expected = "select p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed," +
            " p.occupation, a.address_id, a.street_address, a.city, a.state" +
            " from Person p join Address a on p.address_id = a.address_id" +
            " where p.id < :p1 order by id limit :_limit"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val rows = template.selectMany(selectStatement) { rs, _ ->
            val record = PersonWithAddress()
            record.id = rs.getInt(1)
            record.firstName = rs.getString(2)
            record.lastName = rs.getString(3)
            record.birthDate = rs.getTimestamp(4)
            record.employed = rs.getString(5)
            record.occupation = rs.getString(6)

            val address = AddressRecord()
            record.address = address
            address.id = rs.getInt(7)
            address.streetAddress = rs.getString(8)
            address.city = rs.getString(9)
            address.state = rs.getString(10)

            record
        }


        assertThat(rows.size).isEqualTo(3)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isEqualTo("Yes")
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(address?.id).isEqualTo(1)
            assertThat(address?.streetAddress).isEqualTo("123 Main Street")
            assertThat(address?.city).isEqualTo("Bedrock")
            assertThat(address?.state).isEqualTo("IN")
        }
    }

    @Test
    fun testRawSelectWithComplexWhere1() {
        val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId).from(Person) {
            where(id, isLessThan(5))
            and(id, isLessThan(4)) {
                and(id, isLessThan(3)) {
                    and(id, isLessThan(2))
                }
            }
            orderBy(id)
            limit(3)
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id" +
            " from Person" +
            " where id < :p1" +
            " and (id < :p2" +
            " and (id < :p3 and id < :p4))" +
            " order by id limit :_limit"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val rows = template.selectMany(selectStatement) { rs, _ ->
            val record = PersonRecord()
            record.id = rs.getInt(1)
            record.firstName = rs.getString(2)
            record.lastName = rs.getString(3)
            record.birthDate = rs.getTimestamp(4)
            record.employed = rs.getString(5)
            record.occupation = rs.getString(6)
            record.addressId = rs.getInt(7)
            record
        }

        assertThat(rows.size).isEqualTo(1)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isEqualTo("Yes")
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithComplexWhere2() {
        val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId).from(Person) {
            where(id, isEqualTo(5))
            or(id, isEqualTo(4)) {
                or(id, isEqualTo(3)) {
                    or(id, isEqualTo(2))
                }
            }
            orderBy(id)
            limit(3)
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id" +
            " from Person" +
            " where id = :p1" +
            " or (id = :p2" +
            " or (id = :p3 or id = :p4))" +
            " order by id limit :_limit"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val rows = template.selectMany(selectStatement) { rs, _ ->
            val record = PersonRecord()
            record.id = rs.getInt(1)
            record.firstName = rs.getString(2)
            record.lastName = rs.getString(3)
            record.birthDate = rs.getTimestamp(4)
            record.employed = rs.getString(5)
            record.occupation = rs.getString(6)
            record.addressId = rs.getInt(7)
            record
        }

        assertThat(rows.size).isEqualTo(3)
        with(rows[2]) {
            assertThat(id).isEqualTo(4)
            assertThat(firstName).isEqualTo("Barney")
            assertThat(lastName).isEqualTo("Rubble")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isEqualTo("Yes")
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(2)
        }
    }

    @Test
    fun testRawUpdate1() {
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
        }

        assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
            " set first_name = :p1" +
            " where first_name = :p2")

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testRawUpdate2() {
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred")) {
                or(id, isGreaterThan(3))
            }
        }

        assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
            " set first_name = :p1" +
            " where (first_name = :p2 or id > :p3)")

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testRawUpdate3() {
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            or(id, isEqualTo(5)) {
                or(id, isEqualTo(6))
            }
        }

        assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
            " set first_name = :p1" +
            " where first_name = :p2" +
            " or (id = :p3 or id = :p4)")

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawUpdate4() {
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            and(id, isEqualTo(1)) {
                or(id, isEqualTo(6))
            }
        }

        assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
            " set first_name = :p1" +
            " where first_name = :p2" +
            " and (id = :p3 or id = :p4)")

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testRawUpdate5() {
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            or(id, isEqualTo(3))
        }

        assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
            " set first_name = :p1" +
            " where first_name = :p2" +
            " or id = :p3")

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(2)
    }
}
