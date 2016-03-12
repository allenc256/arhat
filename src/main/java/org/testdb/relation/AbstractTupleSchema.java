package org.testdb.relation;

import org.immutables.value.Value;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractTupleSchema implements TupleSchema {
    @Value.Check
    protected void check() {
        // Check indexes on column schemas.
        for (int i = 0; i < size(); ++i) {
            ColumnSchema cs = getColumnSchema(i);
            Preconditions.checkState(
                    cs.getIndex() == i,
                    "Column '%s' has incorrect index (was %d but expected %d).",
                    cs.getQualifiedName(),
                    cs.getIndex(),
                    i);
        }
        
        // N.B., column names are *not* guaranteed to be unique. For example,
        // "SELECT foo, foo FROM foobar" is a valid SQL query.
    }
}
