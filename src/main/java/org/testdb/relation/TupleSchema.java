package org.testdb.relation;

import java.util.List;

import com.google.common.collect.ListMultimap;

public interface TupleSchema {
    List<ColumnSchema> getColumnSchemas();
    
    ListMultimap<String, ColumnSchema> getColumnSchemasByName();
    
    default ColumnSchema getColumnSchema(int column) {
        return getColumnSchemas().get(column);
    }
    
    default int size() {
        return getColumnSchemas().size();
    }
}
