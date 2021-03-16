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

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.AbstractSingleValueCondition;

public class IsEqualTo<T> extends AbstractSingleValueCondition<T> {

    protected IsEqualTo(T value) {
        super(value);
    }

    @Override
    public String renderCondition(String columnName, String placeholder) {
        return columnName + " = " + placeholder; //$NON-NLS-1$
    }

    public static <T> IsEqualTo<T> of(T value) {
        return new IsEqualTo<>(value);
    }

    /**
     * If renderable and the value matches the predicate, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @deprecated replaced by {@link IsEqualTo#filter(Predicate)}
     * @param predicate predicate applied to the value, if renderable
     * @return this condition if renderable and the value matches the predicate, otherwise a condition
     *     that will not render.
     */
    @Deprecated
    public IsEqualTo<T> when(Predicate<T> predicate) {
        return filter(predicate);
    }

    /**
     * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
     *     condition that will not render (this).
     *
     * @deprecated replaced by {@link IsEqualTo#map(UnaryOperator)}
     * @param mapper a mapping function to apply to the value, if renderable
     * @return a new condition with the result of applying the mapper to the value of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    @Deprecated
    public IsEqualTo<T> then(UnaryOperator<T> mapper) {
        return map(mapper);
    }

    /**
     * If renderable and the value matches the predicate, returns this condition. Else returns a condition
     *     that will not render.
     *
     * @param predicate predicate applied to the value, if renderable
     * @return this condition if renderable and the value matches the predicate, otherwise a condition
     *     that will not render.
     */
    public IsEqualTo<T> filter(Predicate<T> predicate) {
        if (shouldRender()) {
            return predicate.test(value) ? this : EmptyIsEqualTo.empty();
        } else {
            return this;
        }
    }

    /**
     * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
     *     condition that will not render (this).
     *
     * @param mapper a mapping function to apply to the value, if renderable
     * @return a new condition with the result of applying the mapper to the value of this condition,
     *     if renderable, otherwise a condition that will not render.
     */
    public IsEqualTo<T> map(UnaryOperator<T> mapper) {
        return shouldRender() ? new IsEqualTo<>(mapper.apply(value)) : this;
    }

    public static class EmptyIsEqualTo<T> extends IsEqualTo<T> {
        private static final EmptyIsEqualTo<?> EMPTY = new EmptyIsEqualTo<>();

        public static <T> EmptyIsEqualTo<T> empty() {
            @SuppressWarnings("unchecked")
            EmptyIsEqualTo<T> t = (EmptyIsEqualTo<T>) EMPTY;
            return t;
        }

        private EmptyIsEqualTo() {
            super(null);
        }

        @Override
        public boolean shouldRender() {
            return false;
        }
    }
}
