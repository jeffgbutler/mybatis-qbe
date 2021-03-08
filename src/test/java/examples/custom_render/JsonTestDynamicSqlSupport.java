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
package examples.custom_render;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class JsonTestDynamicSqlSupport {
    public static final JsonTest jsonTest = new JsonTest();
    public static final SqlColumn<Integer> id = jsonTest.id;
    public static final SqlColumn<String> description = jsonTest.description;
    public static final SqlColumn<String> info = jsonTest.info;

    public static class JsonTest extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);
        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);
        public final SqlColumn<String> info = column("info", JDBCType.VARCHAR)
                .withRenderingStrategy(new JsonRenderingStrategy());

        public JsonTest() {
            super("JsonTest");
        }
    }
}
