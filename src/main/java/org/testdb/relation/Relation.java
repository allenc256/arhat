package org.testdb.relation;

import java.util.stream.Stream;

public interface Relation {
    TupleSchema getTupleSchema();
    
    Stream<Tuple> getTupleStream();
}
