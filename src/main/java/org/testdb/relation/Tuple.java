package org.testdb.relation;

import java.util.List;

public interface Tuple {
    TupleSchema getSchema();
    
    List<Object> getValues();
    
    default Object get(int columnIndex) {
        return getValues().get(columnIndex);
    }

    default int size() {
        return getSchema().size();
    }
}
