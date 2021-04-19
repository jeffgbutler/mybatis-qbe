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
package org.mybatis.dynamic.sql.util;

import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter for use with MyBatis SQL provider annotations.
 *
 * @author Jeff Butler
 *
 */
public class SqlProviderAdapter {

    public String delete(DeleteStatementProvider deleteStatement) {
        return deleteStatement.getDeleteStatement();
    }

    public String generalInsert(GeneralInsertStatementProvider insertStatement) {
        return insertStatement.getInsertStatement();
    }

    public String insert(InsertStatementProvider<?> insertStatement) {
        return insertStatement.getInsertStatement();
    }

    public String insertMultiple(MultiRowInsertStatementProvider<?> insertStatement) {
        return insertStatement.getInsertStatement();
    }

    public String insertMultipleWithGeneratedKeys(Map<String, Object> parameterMap) {
        List<String> entries = parameterMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith("param")) //$NON-NLS-1$
                .filter(e -> e.getValue() instanceof String)
                .map(e -> (String) e.getValue())
                .collect(Collectors.toList());
        if (entries.size() == 1) {
            return entries.get(0);
        } else {
            throw new RuntimeException("The parameters for insertMultipleWithGeneratedKeys must contain exactly one parameter of type String");
        }
    }

    public String insertSelect(InsertSelectStatementProvider insertStatement) {
        return insertStatement.getInsertStatement();
    }

    public String select(SelectStatementProvider selectStatement) {
        return selectStatement.getSelectStatement();
    }

    public String update(UpdateStatementProvider updateStatement) {
        return updateStatement.getUpdateStatement();
    }
}
