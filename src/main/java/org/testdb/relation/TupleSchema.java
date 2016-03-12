package org.testdb.relation;

import java.util.List;

import com.google.common.collect.Multimap;

public interface TupleSchema {
    List<ColumnSchema> getColumnSchemas();
    
    Multimap<String, ColumnSchema> getColumnSchemasByName();
    
    default ColumnSchema getColumnSchema(int column) {
        return getColumnSchemas().get(column);
    }
    
    default Iterable<ColumnSchema> getColumnSchemas(String column) {
        return getColumnSchemasByName().get(column);
    }
    
    default int size() {
        return getColumnSchemas().size();
    }
}
