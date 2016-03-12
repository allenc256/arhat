package org.testdb.relation;

import java.util.Set;

import org.testdb.type.SqlType;

import com.google.common.base.Optional;

public interface ColumnSchema {
    int getIndex();
    
    Set<String> getQualifierAliases();
    
    Optional<String> getName();
    
    SqlType getType();
}
