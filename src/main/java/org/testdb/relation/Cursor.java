package org.testdb.relation;

import java.util.Iterator;

public interface Cursor<T> extends Iterator<T>, AutoCloseable {
    // empty
}
