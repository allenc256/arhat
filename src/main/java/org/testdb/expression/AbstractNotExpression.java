package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.relation.Tuple;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractNotExpression implements Expression {
    public abstract Expression getSourceExpression();

    @Override
    public Object evaluate(Tuple tuple) {
        Boolean b = (Boolean)getSourceExpression().evaluate(tuple);
        return b != null ? !b : false;
    }

    @Override
    public SqlType getType() {
        return SqlType.BOOLEAN;
    }
    
    @Value.Check
    protected void check() {
        Preconditions.checkState(
                getSourceExpression().getType().equals(SqlType.BOOLEAN),
                "Logical not operator cannot be applied to a non-boolean expression.");
    }
}
