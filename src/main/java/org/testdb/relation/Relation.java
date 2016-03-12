package org.testdb.relation;

public interface Relation {
    TupleSchema getTupleSchema();
    
    Cursor<Tuple> getTuples();
}
