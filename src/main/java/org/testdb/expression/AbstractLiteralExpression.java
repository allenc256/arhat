package org.testdb.expression;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.testdb.relation.Tuple;

@Value.Immutable
public abstract class AbstractLiteralExpression implements Expression {
    @Nullable
    public abstract Object getValue();

    @Override
    public Object evaluate(Tuple tuple) {
        return getValue();
    }
}
