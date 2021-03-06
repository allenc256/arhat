package org.testdb.relation;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractFilteredRelation implements Relation {
    public abstract Relation getSourceRelation();
    
    public abstract Predicate<Tuple> getFilterPredicate();
    
    @Override
    public TupleSchema getTupleSchema() {
        return getSourceRelation().getTupleSchema();
    }

    @Override
    public Stream<Tuple> getTupleStream() {
        return getSourceRelation().getTupleStream().filter(getFilterPredicate());
    }
}
