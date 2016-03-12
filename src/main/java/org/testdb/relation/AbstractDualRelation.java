package org.testdb.relation;

import java.util.stream.Stream;

import org.immutables.value.Value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Value.Immutable(singleton = true)
public abstract class AbstractDualRelation implements Relation {
    @Value.Derived
    @Override
    public TupleSchema getTupleSchema() {
        return ImmutableTupleSchema.builder().build();
    }
    
    @Value.Derived
    public Tuple getEmptyTuple() {
        return ImmutableTuple.builder().values(ImmutableList.of()).build();
    }

    @Override
    public Stream<Tuple> getTupleStream() {
        return ImmutableSet.of(getEmptyTuple()).stream();
    }
}
