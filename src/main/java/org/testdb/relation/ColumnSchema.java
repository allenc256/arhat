package org.testdb.relation;

import org.testdb.type.SqlType;

public interface ColumnSchema {
    int getIndex();
    
    QualifiedName getQualifiedName();
    
    SqlType getType();
}
