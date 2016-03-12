package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.relation.Tuple;

@Value.Immutable
public abstract class AbstractExtractIndexExpression implements Expression {
    abstract int getTupleIndex();

    @Override
    public Object evaluate(Tuple tuple) {
        return tuple.get(getTupleIndex());
    }
}
