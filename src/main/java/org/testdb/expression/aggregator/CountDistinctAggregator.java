package org.testdb.expression.aggregator;

import java.util.Set;

import org.testdb.expression.Expression;
import org.testdb.type.SqlType;

import com.google.common.collect.Sets;

public class CountDistinctAggregator extends ExpressionAggregator<Object, Set<Object>, Integer> {
    public CountDistinctAggregator(Expression inputExpression) {
        super(inputExpression);
    }

    @Override
    protected Set<Object> aggregate(Object value, Set<Object> accumulator) {
        accumulator.add(value);
        return accumulator;
    }
    
    @Override
    public Integer getValue(Set<Object> accumulator) {
        return accumulator.size();
    }

    @Override
    public Set<Object> emptyAccumulator() {
        return Sets.newHashSet();
    }

    @Override
    public SqlType getType() {
        return SqlType.INTEGER;
    }
}
