package org.testdb.relation;

import org.immutables.value.Value;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

@Value.Immutable
public abstract class AbstractFilteredRelation implements Relation {
    public abstract Relation getSourceRelation();
    
    public abstract Predicate<Tuple> getFilterPredicate();
    
    @Override
    public TupleSchema getTupleSchema() {
        return getSourceRelation().getTupleSchema();
    }

    @Override
    public Cursor<Tuple> getTuples() {
        Cursor<Tuple> c = getSourceRelation().getTuples();
        return new CursorAdapter<Tuple>(Iterators.filter(c, getFilterPredicate())) {
            @Override
            public void close() throws Exception {
                c.close();
            }
        };
    }
}
