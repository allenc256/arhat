package org.testdb.relation;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;

public abstract class ForwardingRelation extends ForwardingObject implements Relation {
    @Override
    protected abstract Relation delegate();

    @Override
    public Optional<String> getName() {
        return delegate().getName();
    }

    @Override
    public TupleSchema getTupleSchema() {
        return delegate().getTupleSchema();
    }

    @Override
    public Cursor<Tuple> getTuples() {
        return delegate().getTuples();
    }
}
