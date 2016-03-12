package org.testdb.relation;

import java.util.stream.Stream;

public interface IndexedRelation extends Relation {
    Stream<Tuple> getTuples(TupleRange range);
}
