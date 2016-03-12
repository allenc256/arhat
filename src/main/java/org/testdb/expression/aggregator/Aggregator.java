package org.testdb.expression.aggregator;

import org.testdb.relation.Tuple;
import org.testdb.type.SqlType;

public interface Aggregator<A, V> {
    A aggregate(Tuple tuple, A accumulator);
    
    V getValue(A accumulator);
    
    default A emptyAccumulator() {
        return null;
    }
    
    default A reduce(A a1, A a2) {
        throw new UnsupportedOperationException();
    }
    
    SqlType getType();
}
