/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.where.render.CriterionRenderer;
import org.mybatis.dynamic.sql.where.render.FragmentAndParameters;

public abstract class AbstractWhereBuilder<T extends AbstractWhereBuilder<T>> {
    private List<CriterionWrapper> criteria = new ArrayList<>();
    private int valueCount = 1;
    
    protected <S> AbstractWhereBuilder(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.of(column, condition, subCriteria);
        addCriterion(criterion);
    }

    public <S> T and(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.of("and", column, condition, subCriteria); //$NON-NLS-1$
        addCriterion(criterion);
        return getThis();
    }
    
    public <S> T or(SqlColumn<S> column, Condition<S> condition, SqlCriterion<?>...subCriteria) {
        SqlCriterion<S> criterion = SqlCriterion.of("or", column, condition, subCriteria); //$NON-NLS-1$
        addCriterion(criterion);
        return getThis();
    }
    
    private <S> void addCriterion(SqlCriterion<S> criterion) {
        criteria.add(CriterionWrapper.of(criterion, valueCount));
        valueCount += criterion.valueCount();
    }
    
    protected WhereSupport renderCriteria(Function<SqlColumn<?>, String> nameFunction) {
        return criteria.stream().collect(Collector.of(
                () -> new WhereCollectorSupport(nameFunction),
                WhereCollectorSupport::add,
                WhereCollectorSupport::merge,
                WhereCollectorSupport::getWhereSupport));
    }
    
    protected abstract T getThis();
    
    static class CriterionWrapper {
        SqlCriterion<?> criterion;
        AtomicInteger sequence;
        
        static CriterionWrapper of(SqlCriterion<?> criterion, int idStartValue) {
            CriterionWrapper wrapper = new CriterionWrapper();
            wrapper.criterion = criterion;
            wrapper.sequence = new AtomicInteger(idStartValue);
            return wrapper;
        }
    }
    
    static class WhereCollectorSupport {
        Map<String, Object> parameters = new HashMap<>();
        List<String> phrases = new ArrayList<>();
        Function<SqlColumn<?>, String> nameFunction;
        
        WhereCollectorSupport(Function<SqlColumn<?>, String> nameFunction) {
            this.nameFunction = nameFunction;
        }
        
        void add(CriterionWrapper criterionWrapper) {
            FragmentAndParameters rc = CriterionRenderer.of(criterionWrapper.criterion, criterionWrapper.sequence, nameFunction)
                    .render();
            phrases.add(rc.fragment());
            parameters.putAll(rc.parameters());
        }
        
        WhereCollectorSupport merge(WhereCollectorSupport other) {
            parameters.putAll(other.parameters);
            phrases.addAll(other.phrases);
            return this;
        }
        
        WhereSupport getWhereSupport() {
            String whereClause = phrases.stream().collect(Collectors.joining(" ", "where ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return WhereSupport.of(whereClause, parameters);
        }
    }
}