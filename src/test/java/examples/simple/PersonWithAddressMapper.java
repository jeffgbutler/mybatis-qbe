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
package examples.simple;

import static examples.simple.AddressDynamicSqlSupport.address;
import static examples.simple.PersonDynamicSqlSupport.birthDate;
import static examples.simple.PersonDynamicSqlSupport.employed;
import static examples.simple.PersonDynamicSqlSupport.firstName;
import static examples.simple.PersonDynamicSqlSupport.id;
import static examples.simple.PersonDynamicSqlSupport.lastName;
import static examples.simple.PersonDynamicSqlSupport.occupation;
import static examples.simple.PersonDynamicSqlSupport.person;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectListHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectOneHelper;

/**
 * 
 * This is a mapper that shows coding a join 
 *
 */
@Mapper
public interface PersonWithAddressMapper {
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="PersonWithAddressResult", value= {
            @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR, typeHandler=LastNameTypeHandler.class),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR),
            @Result(column="address_id", property="address.id", jdbcType=JdbcType.INTEGER),
            @Result(column="street_address", property="address.streetAddress", jdbcType=JdbcType.VARCHAR),
            @Result(column="city", property="address.city", jdbcType=JdbcType.VARCHAR),
            @Result(column="state", property="address.state", jdbcType=JdbcType.CHAR)
    })
    List<PersonWithAddress> selectMany(SelectStatementProvider selectStatement);
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("PersonWithAddressResult")
    Optional<PersonWithAddress> selectOne(SelectStatementProvider selectStatement);

    static BasicColumn[] selectList() {
        return new BasicColumn[] {id.as("A_ID"), firstName, lastName, birthDate, employed, occupation, address.id,
                address.streetAddress, address.city, address.state};
    }
    
    default Optional<PersonWithAddress> selectOne(MyBatis3SelectOneHelper<PersonWithAddress> helper) {
        return helper.apply(SelectDSL.selectWithMapper(this::selectOne, selectList())
                .from(person).join(address).on(person.addressId, equalTo(address.id)))
                .build()
                .execute();
    }
    
    default List<PersonWithAddress> select(MyBatis3SelectListHelper<PersonWithAddress> helper) {
        return helper.apply(SelectDSL.selectWithMapper(this::selectMany, selectList())
                .from(person).fullJoin(address).on(person.addressId, equalTo(address.id)))
                .build()
                .execute();
    }
    
    default Optional<PersonWithAddress> selectByPrimaryKey(Integer id_) {
        return selectOne(h -> 
            h.where(id, isEqualTo(id_))
        );
    }
}
