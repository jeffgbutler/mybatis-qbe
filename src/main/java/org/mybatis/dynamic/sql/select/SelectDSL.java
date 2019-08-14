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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGatherer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Implements a standard SQL dialect for building model classes.
 * 
 * @author Jeff Butler
 *
 * @param <R> the type of model produced by this builder
 */
public class SelectDSL<R> implements Buildable<R> {

    private Function<SelectModel, R> adapterFunction;
    private List<QueryExpressionDSL<R>> queryExpressions = new ArrayList<>();
    private OrderByModel orderByModel;
    private Supplier<R> buildDelegateMethod;
    private Long limit;
    private Long offset;
    private Long fetchFirstRows;
    
    private SelectDSL(Function<SelectModel, R> adapterFunction) {
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
        buildDelegateMethod = this::internalBuild;
    }

    public static QueryExpressionDSL.FromGatherer<SelectModel> select(BasicColumn...selectList) {
        return select(Function.identity(), selectList);
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> select(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        return new FromGatherer.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .build();
    }
    
    public static QueryExpressionDSL.FromGatherer<SelectModel> selectDistinct(BasicColumn...selectList) {
        return selectDistinct(Function.identity(), selectList);
    }
    
    public static <R> QueryExpressionDSL.FromGatherer<R> selectDistinct(Function<SelectModel, R> adapterFunction,
            BasicColumn...selectList) {
        return new FromGatherer.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL<>(adapterFunction))
                .isDistinct()
                .build();
    }
    
    public static <T> QueryExpressionDSL.FromGatherer<MyBatis3SelectModelAdapter<T>> selectWithMapper(
            Function<SelectStatementProvider, T> mapperMethod, BasicColumn...selectList) {
        return select(selectModel -> MyBatis3SelectModelAdapter.of(selectModel, mapperMethod), selectList);
    }
    
    public static <T> QueryExpressionDSL.FromGatherer<MyBatis3SelectModelAdapter<T>> selectDistinctWithMapper(
            Function<SelectStatementProvider, T> mapperMethod, BasicColumn...selectList) {
        return selectDistinct(selectModel -> MyBatis3SelectModelAdapter.of(selectModel, mapperMethod),
                selectList);
    }
    
    public QueryExpressionDSL<R> newQueryExpression(FromGatherer<R> fromGatherer) {
        QueryExpressionDSL<R> queryExpression = new QueryExpressionDSL<>(fromGatherer);
        queryExpressions.add(queryExpression);
        return queryExpression;
    }
    
    public QueryExpressionDSL<R> newQueryExpression(FromGatherer<R> fromGatherer, String tableAlias) {
        QueryExpressionDSL<R> queryExpression = new QueryExpressionDSL<>(fromGatherer, tableAlias);
        queryExpressions.add(queryExpression);
        return queryExpression;
    }
    
    void setOrderByModel(OrderByModel orderByModel) {
        this.orderByModel = orderByModel;
    }
    
    public LimitFinisher limit(long limit) {
        this.limit = limit;
        return new LimitFinisher();
    }

    public OffsetFirstFinisher offset(long offset) {
        this.offset = offset;
        return new OffsetFirstFinisher();
    }

    public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return new FetchFirstFinisher();
    }

    @Override
    public R build() {
        return buildDelegateMethod.get();
    }
    
    private R internalBuild() {
        SelectModel selectModel = SelectModel.withQueryExpressions(buildModels())
                .withOrderByModel(orderByModel)
                .withPagingModel(buildPagingModel())
                .build();
        return adapterFunction.apply(selectModel);
    }
    
    private List<QueryExpressionModel> buildModels() {
        return queryExpressions.stream()
                .map(QueryExpressionDSL::buildModel)
                .collect(Collectors.toList());
    }
    
    private PagingModel buildPagingModel() {
        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }
    
    public class LimitFinisher implements Buildable<R> {
        
        public LimitFinisher() {
            buildDelegateMethod = this::internalBuild;
        }
        
        public OffsetFinisher offset(long offset) {
            SelectDSL.this.offset = offset;
            return new OffsetFinisher();
        }
        
        @Override
        public R build() {
            return buildDelegateMethod.get();
        }
        
        private R internalBuild() {
            return SelectDSL.this.internalBuild();
        }
    }

    public class OffsetFinisher implements Buildable<R> {
        public OffsetFinisher() {
            buildDelegateMethod = this::internalBuild;
        }
        
        @Override
        public R build() {
            return buildDelegateMethod.get();
        }
        
        private R internalBuild() {
            return SelectDSL.this.internalBuild();
        }
    }

    public class OffsetFirstFinisher implements Buildable<R> {
        public OffsetFirstFinisher() {
            buildDelegateMethod = this::internalBuild;
        }
        
        public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            SelectDSL.this.fetchFirstRows = fetchFirstRows;
            return new FetchFirstFinisher();
        }
        
        @Override
        public R build() {
            return buildDelegateMethod.get();
        }
        
        private R internalBuild() {
            return SelectDSL.this.internalBuild();
        }
    }
    
    public class FetchFirstFinisher {
        public FetchFirstFinisher() {
            super();
        }
        
        public RowsOnlyFinisher rowsOnly() {
            return new RowsOnlyFinisher();
        }
    }
    
    public class RowsOnlyFinisher implements Buildable<R> {
        public RowsOnlyFinisher() {
            buildDelegateMethod = this::internalBuild;
        }
        
        @Override
        public R build() {
            return buildDelegateMethod.get();
        }
        
        private R internalBuild() {
            return SelectDSL.this.internalBuild();
        }
    }
}
