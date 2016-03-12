package org.testdb.relation;

import org.testdb.type.SqlType;

import com.google.common.base.Optional;

public interface ColumnSchema {
    int getIndex();
    
    Optional<QualifiedName> getQualifiedName();
    
    SqlType getType();
}
