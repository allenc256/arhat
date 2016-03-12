package org.testdb.relation;

import com.google.common.base.Optional;

public interface Relation {
    Optional<String> getName();
    
    TupleSchema getTupleSchema();
    
    Cursor<Tuple> getTuples();
}
