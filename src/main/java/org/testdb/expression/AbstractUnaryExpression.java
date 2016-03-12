package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.relation.Tuple;

@Value.Immutable
public abstract class AbstractUnaryExpression implements Expression {
    public abstract Expression getInputExpression();
    
    public abstract UnaryOperator<?, ?> getOperator();
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object evaluate(Tuple tuple) {
        UnaryOperator op = getOperator();
        return op.apply(getInputExpression().evaluate(tuple));
    }
}
