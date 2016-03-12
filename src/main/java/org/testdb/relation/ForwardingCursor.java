package org.testdb.relation;

import com.google.common.collect.ForwardingIterator;

public abstract class ForwardingCursor<T> extends ForwardingIterator<T> implements Cursor<T> {
    @Override
    protected abstract Cursor<T> delegate();
    
    @Override
    public void close() throws Exception {
        delegate().close();
    }
}
