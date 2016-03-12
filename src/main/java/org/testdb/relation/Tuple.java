package org.testdb.relation;

import java.util.List;

public interface Tuple {
    List<Object> getValues();
    
    default Object get(int columnIndex) {
        return getValues().get(columnIndex);
    }

    default int size() {
        return getValues().size();
    }
}
