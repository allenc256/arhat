package org.testdb.relation;

public interface IndexedRelation extends Relation {
    Cursor<Tuple> getTuples(TupleRange range);
}
