package org.testdb.relation;

import java.util.stream.Stream;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractNestedLoopJoinRelation implements Relation {
    public abstract Relation getLeftRelation();
    
    public abstract Relation getRightRelation();
    
    @Value.Derived
    @Override
    public TupleSchema getTupleSchema() {
        return TupleSchemas.concat(
                getLeftRelation().getTupleSchema(),
                getRightRelation().getTupleSchema());
    }

    @Override
    public Stream<Tuple> getTupleStream() {
        return getLeftRelation().getTupleStream().flatMap(this::getJoinedTuples);
    }
    
    private Stream<Tuple> getJoinedTuples(Tuple leftTuple) {
        return getRightRelation()
                .getTupleStream()
                .map(rightTuple -> concatenateTuples(leftTuple, rightTuple));
    }
    
    private Tuple concatenateTuples(Tuple t1, Tuple t2) {
        return ImmutableConcatenatedTuple.builder()
                .leftTuple(t1)
                .rightTuple(t2)
                .build();
    }
}
