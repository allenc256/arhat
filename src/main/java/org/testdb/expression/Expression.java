package org.testdb.expression;

import org.testdb.relation.Tuple;

public interface Expression<T> {
    T evaluate(Tuple tuple);
}
