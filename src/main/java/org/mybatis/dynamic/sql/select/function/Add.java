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

import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class Add<T extends Number, S extends BaseMultipleColumnFunction<T, S>> extends BaseMultipleColumnFunction<T, S> {
    
    private Add(List<BindableColumn<T>> columns) {
        super(columns);
    }
    
    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return columns.stream()
                .map(column -> column.renderWithTableAlias(tableAliasCalculator))
                .collect(Collectors.joining(" + ", "(", ")"));
    }
    
    public static <T extends Number, S extends BaseMultipleColumnFunction<T, S>> Add<T, S> of(List<BindableColumn<T>> columns) {
        return new Add<>(columns);
    }
    
    @Override
    protected Add<T, S> copyWithColumn(List<BindableColumn<T>> columns, BaseMultipleColumnFunction<T, S> otherOperation) {
        return new Add<>(columns);
    }
}
