package org.testdb.expression;

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractLiteralExpression implements Expression {
    @Nullable
    public abstract Object getValue();

    @Override
    public Object evaluate(Environment env) {
        return getValue();
    }
}
