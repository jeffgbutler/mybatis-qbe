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
package org.mybatis.dynamic.sql.where.render;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.ColumnBasedCriterion;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlCriterionVisitor;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

/**
 * Renders a {@link SqlCriterion} to a {@link RenderedCriterion}. The process is complex because all conditions
 * may or may not be a candidate for rendering. For example, "isEqualWhenPresent" will not render when the value
 * is null. It is also complex because SqlCriterion may or may not include sub-criteria.
 *
 * <p>Rendering is a recursive process. The renderer will recurse into each sub-criteria - which may also
 * contain further sub-criteria - until all possible sub-criteria are rendered into a single fragment. So, for example,
 * the fragment may end up looking like:
 *
 * <pre>
 *     col1 = ? and (col2 = ? or (col3 = ? and col4 = ?))
 * </pre>
 *
 * <p>It is also possible that the end result will be empty if all criteria and sub-criteria are not valid for
 * rendering.
 *
 * @author Jeff Butler
 */
public class CriterionRenderer implements SqlCriterionVisitor<Optional<RenderedCriterion>> {
    private final AtomicInteger sequence;
    private final RenderingStrategy renderingStrategy;
    private final TableAliasCalculator tableAliasCalculator;
    private final String parameterName;

    private CriterionRenderer(Builder builder) {
        sequence = Objects.requireNonNull(builder.sequence);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        parameterName = builder.parameterName;
    }

    @Override
    public <T> Optional<RenderedCriterion> visit(ColumnBasedCriterion<T> criterion) {
        RenderedCriterion rc;
        if (criterion.condition().shouldRender()) {
            rc = renderWithInitialCondition(renderCondition(criterion), criterion);
        } else {
            rc = renderWithoutInitialCondition(criterion);
        }
        return Optional.ofNullable(rc);
    }

    @Override
    public Optional<RenderedCriterion> visit(ExistsCriterion criterion) {
        SelectStatementProvider selectStatement = SelectRenderer.withSelectModel(criterion.selectModel())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();

        FragmentAndParameters fp = FragmentAndParameters
                .withFragment("exists (" + selectStatement.getSelectStatement() + ")") //$NON-NLS-1$ //$NON-NLS-2$
                .withParameters(selectStatement.getParameters())
                .build();

        return Optional.of(renderWithInitialCondition(fp, criterion));
    }

    private <T> FragmentAndParameters renderCondition(ColumnBasedCriterion<T> criterion) {
        WhereConditionVisitor<T> visitor = WhereConditionVisitor.withColumn(criterion.column())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withTableAliasCalculator(tableAliasCalculator)
                .withParameterName(parameterName)
                .build();
        return criterion.condition().accept(visitor);
    }

    private List<RenderedCriterion> renderSubCriteria(SqlCriterion criterion) {
        return criterion.mapSubCriteria(c -> c.accept(this))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private RenderedCriterion renderWithoutInitialCondition(SqlCriterion criterion) {
        List<RenderedCriterion> subCriteria = renderSubCriteria(criterion);
        if (subCriteria.isEmpty()) {
            return null;
        }

        return calculateRenderedCriterion(subCriteria, criterion);
    }

    private RenderedCriterion renderWithInitialCondition(FragmentAndParameters initialCondition,
            SqlCriterion criterion) {
        List<RenderedCriterion> subCriteria = renderSubCriteria(criterion);
        if (subCriteria.isEmpty()) {
            return calculateRenderedCriterion(initialCondition, criterion);
        }

        return calculateRenderedCriterion(initialCondition, criterion, subCriteria);
    }

    private RenderedCriterion calculateRenderedCriterion(FragmentAndParameters initialCondition,
                                                         SqlCriterion criterion) {
        return fromFragmentAndParameters(initialCondition, criterion);
    }

    private RenderedCriterion calculateRenderedCriterion(List<RenderedCriterion> subCriteria,
                                                         SqlCriterion criterion) {
        return calculateRenderedCriterion(subCriteria.get(0).fragmentAndParameters(),
                criterion,
                subCriteria.subList(1, subCriteria.size()));
    }

    private RenderedCriterion calculateRenderedCriterion(FragmentAndParameters initialCondition,
            SqlCriterion criterion,
            List<RenderedCriterion> subCriteria) {
        FragmentCollector fc = subCriteria.stream()
                .map(RenderedCriterion::fragmentAndParametersWithConnector)
                .collect(FragmentCollector.collect(initialCondition));
        return fromFragmentAndParameters(FragmentAndParameters.withFragment(calculateFragment(fc))
                .withParameters(fc.parameters())
                .build(), criterion);
    }

    private String calculateFragment(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.fragments()
                    .collect(Collectors.joining(" ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.fragments().findFirst().orElse(""); //$NON-NLS-1$
        }
    }

    private RenderedCriterion fromFragmentAndParameters(FragmentAndParameters fragmentAndParameters,
                                                        SqlCriterion criterion) {
        RenderedCriterion.Builder builder = new RenderedCriterion.Builder()
                .withFragmentAndParameters(fragmentAndParameters);

        criterion.connector().ifPresent(builder::withConnector);

        return builder.build();
    }

    public static class Builder {
        private AtomicInteger sequence;
        private RenderingStrategy renderingStrategy;
        private TableAliasCalculator tableAliasCalculator;
        private String parameterName;

        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public Builder withParameterName(String parameterName) {
            this.parameterName = parameterName;
            return this;
        }

        public CriterionRenderer build() {
            return new CriterionRenderer(this);
        }
    }
}
