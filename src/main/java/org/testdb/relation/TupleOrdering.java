package org.testdb.relation;

import java.util.Comparator;

public interface TupleOrdering extends Comparator<Tuple> {
    boolean isLexicoGraphic();
}
