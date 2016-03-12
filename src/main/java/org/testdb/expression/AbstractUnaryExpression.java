package org.testdb.expression;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractUnaryExpression implements Expression {
    public abstract Expression getInputExpression();
    
    public abstract UnaryOperator<?, ?> getOperator();
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object evaluate(Environment env) {
        UnaryOperator op = getOperator();
        return op.apply(getInputExpression().evaluate(env));
    }
}
