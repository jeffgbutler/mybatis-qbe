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
package org.mybatis.dynamic.sql.delete;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

public class DeleteDSL<R> implements Buildable<R> {

    private Function<DeleteModel, R> adapterFunction;
    private SqlTable table;
    protected Optional<DeleteWhereBuilder> whereBuilder = Optional.empty();
    
    private DeleteDSL(SqlTable table, Function<DeleteModel, R> adapterFunction) {
        this.table = Objects.requireNonNull(table);
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }
    
    public DeleteWhereBuilder where() {
        whereBuilder = Optional.of(new DeleteWhereBuilder());
        return whereBuilder.get();
    }
    
    public <T> DeleteWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
        whereBuilder = Optional.of(new DeleteWhereBuilder(column, condition));
        return whereBuilder.get();
    }
    
    public <T> DeleteWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        whereBuilder = Optional.of(new DeleteWhereBuilder(column, condition, subCriteria));
        return whereBuilder.get();
    }
    
    /**
     * WARNING! Calling this method could result in an delete statement that deletes
     * all rows in a table.
     * 
     * @return the model class
     */
    @Override
    public R build() {
        DeleteModel deleteModel = DeleteModel.withTable(table)
                .withWhereModel(whereBuilder.map(DeleteWhereBuilder::buildWhereModel))
                .build();
        return adapterFunction.apply(deleteModel);
    }
    
    public static <R> DeleteDSL<R> deleteFrom(Function<DeleteModel, R> adapterFunction, SqlTable table) {
        return new DeleteDSL<>(table, adapterFunction);
    }
    
    public static DeleteDSL<DeleteModel> deleteFrom(SqlTable table) {
        return deleteFrom(Function.identity(), table);
    }
    
    public static <T> DeleteDSL<MyBatis3DeleteModelAdapter<T>> deleteFromWithMapper(
            Function<DeleteStatementProvider, T> mapperMethod, SqlTable table) {
        return deleteFrom(deleteModel -> MyBatis3DeleteModelAdapter.of(deleteModel, mapperMethod), table);
    }
    
    public class DeleteWhereBuilder extends AbstractWhereDSL<DeleteWhereBuilder> implements Buildable<R> {
        
        private <T> DeleteWhereBuilder() {
            super();
        }
        
        private <T> DeleteWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        private <T> DeleteWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        @Override
        public R build() {
            return DeleteDSL.this.build();
        }
        
        @Override
        protected DeleteWhereBuilder getThis() {
            return this;
        }
    }
}
