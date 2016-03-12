package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.relation.Tuple;

@Value.Immutable
public abstract class AbstractEqualExpression implements Expression<Boolean> {
    public abstract Expression<? extends Object> getLeftExpression();
    
    public abstract Expression<? extends Object> getRightExpression();

    @Override
    public Boolean evaluate(Tuple tuple) {
        Object left = getLeftExpression().evaluate(tuple);
        Object right = getRightExpression().evaluate(tuple);
        
        if (left == null || right == null) {
            return false;
        } else {
            return left.equals(right);
        }
    }
}
