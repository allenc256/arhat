package org.testdb.relation;

import java.util.List;
import java.util.stream.Stream;

import org.immutables.value.Value;
import org.testdb.expression.Expression;
import org.testdb.expression.Expressions;

@Value.Immutable
public abstract class AbstractProjectedRelation implements Relation {
    public abstract List<Expression> getTargetExpressions();
    
    public abstract Relation getSourceRelation();
    
    @Override
    public Stream<Tuple> getTupleStream() {
        return getSourceRelation()
                .getTupleStream()
                .map(tuple -> Expressions.evaluate(tuple, getTargetExpressions()));
    }
    
    @Value.Check
    protected void check() {
        Expressions.checkSchema(getTupleSchema(), getTargetExpressions());
    }
}
