package org.testdb.relation;

import com.google.common.base.Optional;
import com.google.common.collect.BoundType;

public interface TupleRange {
    TupleSchema getTupleSchema();
    
    Optional<Tuple> getLowerBound();
    
    BoundType getLowerBoundType();
    
    Optional<Tuple> getUpperBound();
    
    BoundType getUpperBoundType();
}
