package org.testdb.relation;

import java.util.stream.Stream;

import org.immutables.value.Value;
import org.testdb.expression.Expressions;

@Value.Immutable
public abstract class AbstractProjectedRelation extends AbstractProjectionOrGroupByRelation {
    @Override
    public Stream<Tuple> getTupleStream() {
        return getSourceRelation()
                .getTupleStream()
                .map(tuple -> Expressions.evaluate(tuple, getTargetExpressions()));
    }
}
