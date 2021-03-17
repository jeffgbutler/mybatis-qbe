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
package org.mybatis.dynamic.sql.where.condition;

import java.util.function.BooleanSupplier;

import org.mybatis.dynamic.sql.AbstractNoValueCondition;

public class IsNotNull<T> extends AbstractNoValueCondition<T> {
    private static final IsNotNull<?> EMPTY = new IsNotNull<Object>() {
        @Override
        public boolean shouldRender() {
            return false;
        }
    };

    public static <T> IsNotNull<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotNull<T> t = (IsNotNull<T>) EMPTY;
        return t;
    }

    public IsNotNull() {
        super();
    }

    @Override
    public String renderCondition(String columnName) {
        return columnName + " is not null"; //$NON-NLS-1$
    }

    /**
     * If renderable and the supplier returns true, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @deprecated replaced by {@link IsNotNull#filter(BooleanSupplier)}
     * @param booleanSupplier function that specifies whether the condition should render
     * @param <S> condition type - not used except for compilation compliance
     * @return this condition if renderable and the supplier returns true, otherwise a condition
     *     that will not render.
     */
    @Deprecated
    public <S> IsNotNull<S> when(BooleanSupplier booleanSupplier) {
        return filter(booleanSupplier);
    }

    /**
     * If renderable and the supplier returns true, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @param booleanSupplier function that specifies whether the condition should render
     * @param <S> condition type - not used except for compilation compliance
     * @return this condition if renderable and the supplier returns true, otherwise a condition
     *     that will not render.
     */
    public <S> IsNotNull<S> filter(BooleanSupplier booleanSupplier) {
        @SuppressWarnings("unchecked")
        IsNotNull<S> self = (IsNotNull<S>) this;
        return filter(booleanSupplier, IsNotNull::empty, self);
    }
}
