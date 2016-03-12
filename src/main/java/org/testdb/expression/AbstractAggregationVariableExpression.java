package org.testdb.expression;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractAggregationVariableExpression implements Expression {
    public abstract int getVariableIndex();
    
    @Override
    public Object evaluate(Environment env) {
        return env.getAggregationValue(getVariableIndex());
    }
}
