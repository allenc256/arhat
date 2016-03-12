package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.parse.SqlStrings;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractAggregationVariableExpression implements NamedExpression {
    public abstract int getVariableIndex();
    
    @Override
    public Object evaluate(Environment env) {
        return env.getAggregationValue(getVariableIndex());
    }
    
    @Value.Check
    protected void check() {
        Preconditions.checkState(
                SqlStrings.isLowerCase(getName()),
                "Expression name must be lower case.");
    }
}
