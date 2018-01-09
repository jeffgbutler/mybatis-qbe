/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.select.function;

import java.sql.JDBCType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class Substract<T extends Number> implements BindableColumn<T> {
    
    private String alias;
    private List<BindableColumn<T>> columns;
    
    private Substract(List<BindableColumn<T>> columns) {
        this.columns = Objects.requireNonNull(columns);
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return columns.stream()
                .map(column -> column.renderWithTableAlias(tableAliasCalculator))
                .collect(Collectors.joining(" - ", "(", ")"));
    }

    @Override
    public BindableColumn<T> as(String alias) {
        Substract<T> newColumn = new Substract<>(columns);
        newColumn.alias = alias;
        return newColumn;
    }

    @Override
    public JDBCType jdbcType() {
        return columns.get(0).jdbcType();
    }

    @Override
    public Optional<String> typeHandler() {
        return columns.get(0).typeHandler();
    }

    public static <T extends Number> Substract<T> of(List<BindableColumn<T>> columns) {
        return new Substract<>(columns);
    }
}
