package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.relation.Tuple;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractBinaryExpression implements Expression {
    public abstract Expression getLeftExpression();
    
    public abstract Expression getRightExpression();
    
    public abstract BinaryOperator<?, ?> getOperator();
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object evaluate(Tuple tuple) {
        return ((BinaryOperator)getOperator()).apply(
                getLeftExpression().evaluate(tuple),
                getRightExpression().evaluate(tuple));
    }

    @Value.Check
    protected void check() {
        SqlType left = getLeftExpression().getType();
        SqlType right = getRightExpression().getType();
        Preconditions.checkState(
                left.equals(SqlType.NULL) || right.equals(SqlType.NULL) || left.equals(right),
                "Cannot comparison on expressions with incompatible types (left type %s, right type %s).",
                left,
                right);
    }
}
