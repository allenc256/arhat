package org.testdb.relation;

public interface ColumnSchema {
    int getIndex();
    
    String getName();
    
    Class<?> getType();
}
