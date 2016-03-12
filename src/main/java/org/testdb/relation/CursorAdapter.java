package org.testdb.relation;

import java.util.Iterator;

public class CursorAdapter<T> implements Cursor<T> {
    private final Iterator<T> delegate;
    
    public CursorAdapter(Iterator<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        return delegate.next();
    }

    @Override
    public void close() throws Exception {
        // can be overridden in subclasses.
    }
}
