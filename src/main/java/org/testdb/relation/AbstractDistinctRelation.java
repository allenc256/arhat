package org.testdb.relation;

import java.util.stream.Stream;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractDistinctRelation implements Relation {
    public abstract Relation getSourceRelation();
    
    @Override
    public TupleSchema getTupleSchema() {
        return getSourceRelation().getTupleSchema();
    }

    @Override
    public Stream<Tuple> getTupleStream() {
        return getSourceRelation().getTupleStream().distinct();
    }
}
