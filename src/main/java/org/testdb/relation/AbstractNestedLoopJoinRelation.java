package org.testdb.relation;

import java.util.Iterator;

import org.immutables.value.Value;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;

@Value.Immutable
public abstract class AbstractNestedLoopJoinRelation implements Relation {
    public abstract Relation getFromRelation();
    
    public abstract Relation getToRelation();
    
    public abstract JoinPredicate getJoinPredicate();
    
    @Value.Derived
    @Override
    public TupleSchema getTupleSchema() {
        return TupleSchemas.concat(
                getFromRelation().getTupleSchema(),
                getToRelation().getTupleSchema());
    }

    @Override
    public Cursor<Tuple> getTuples() {
        return new JoinCursor();
    }
    
    private class JoinCursor implements Cursor<Tuple> {
        private final Cursor<Tuple> c1;
        private       Cursor<Tuple> c2;
        private final Iterator<Tuple> result;
        
        public JoinCursor() {
            this.c1 = getFromRelation().getTuples();
            this.result = Iterators.concat(Iterators.transform(c1, t1 -> {
                // Close any previous cursor we had open.
                if (c2 != null) {
                    try {
                        c2.close();
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                }

                // Open a new cursor on the "to" relation.
                c2 = getToRelation().getTuples();

                // Find all tuples which match "t1".
                Iterator<Tuple> matchedT2 = Iterators.filter(
                        c2,
                        t2 -> getJoinPredicate().matches(t1, t2));

                // Return concatenated "t1" + "t2" tuples.
                return Iterators.transform(matchedT2, t2 -> {
                    return ImmutableConcatenatedTuple.builder()
                            .leftTuple(t1)
                            .rightTuple(t2)
                            .build();
                });
            }));
        }
        
        @Override
        public boolean hasNext() {
            return result.hasNext();
        }
        
        @Override
        public Tuple next() {
            return result.next();
        }
        
        @Override
        public void close() throws Exception {
            c1.close();
            if (c2 != null) {
                c2.close();
            }
        }
    }
}
