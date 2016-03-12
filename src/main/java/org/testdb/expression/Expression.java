package org.testdb.expression;

import org.testdb.type.SqlType;

public interface Expression {
    Object evaluate(Environment env);
    
    SqlType getType();
}
