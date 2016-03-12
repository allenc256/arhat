package org.testdb.relation;

import java.util.stream.Stream;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingRelation extends ForwardingObject implements Relation {
    @Override
    protected abstract Relation delegate();

    @Override
    public TupleSchema getTupleSchema() {
        return delegate().getTupleSchema();
    }

    @Override
    public Stream<Tuple> getTupleStream() {
        return delegate().getTupleStream();
    }
}
