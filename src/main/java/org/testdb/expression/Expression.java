package org.testdb.expression;

import org.testdb.relation.Tuple;
import org.testdb.type.SqlType;

public interface Expression {
    Object evaluate(Tuple tuple);
    
    SqlType getType();
}
