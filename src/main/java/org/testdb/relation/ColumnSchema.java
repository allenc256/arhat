package org.testdb.relation;

import org.testdb.type.SqlType;

public interface ColumnSchema {
    int getIndex();
    
    String getName();
    
    SqlType getType();
}
