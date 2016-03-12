package org.testdb.relation;

import java.util.List;

import org.testdb.expression.Environment;

public interface Tuple extends Environment {
    List<Object> getValues();
    
    default Object get(int columnIndex) {
        return getValues().get(columnIndex);
    }

    default int size() {
        return getValues().size();
    }
}
