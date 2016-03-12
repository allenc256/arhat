package org.testdb.relation;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Cursors {
    private Cursors() {
        // empty
    }
    
    public static <F, T> Cursor<T> transform(Cursor<F> cursor,
                                             Function<? super F, ? extends T> function) {
        Iterator<T> it = Iterators.transform(cursor, function);
        return new CursorAdapter<T>(it) {
            @Override
            public void close() throws Exception {
                cursor.close();
            }
        };
    }
}
