package org.testdb.relation;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingRelation extends ForwardingObject implements Relation {
    @Override
    protected abstract Relation delegate();

    @Override
    public TupleSchema getTupleSchema() {
        return delegate().getTupleSchema();
    }

    @Override
    public Cursor<Tuple> getTuples() {
        return delegate().getTuples();
    }
}
