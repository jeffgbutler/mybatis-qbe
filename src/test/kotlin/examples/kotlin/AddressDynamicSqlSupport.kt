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

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import java.sql.JDBCType

object AddressDynamicSqlSupport {
    object Address : SqlTable("Address") {
        // the 'as' casts are to avoid a warning related to platform types
        val id = column<Int>("id", JDBCType.INTEGER) as SqlColumn<Int>
        val streetAddress = column<String>("street_address", JDBCType.VARCHAR) as SqlColumn<String>
        val city = column<String>("city", JDBCType.VARCHAR) as SqlColumn<String>
        val state = column<String>("state", JDBCType.VARCHAR) as SqlColumn<String>
    }
}
