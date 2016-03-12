package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.relation.Tuple;

import com.sun.istack.internal.Nullable;

@Value.Immutable
public abstract class AbstractLiteralExpression implements Expression<Object> {
    @Nullable
    public abstract Object getValue();

    @Override
    public Object evaluate(Tuple tuple) {
        return getValue();
    }
}
