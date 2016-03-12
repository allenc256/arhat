package org.testdb.relation;

import org.immutables.value.Value;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;

@Value.Immutable
public abstract class AbstractTupleSchema implements TupleSchema {
    @Value.Derived
    public Multimap<String, ColumnSchema> getColumnSchemasByName() {
        ImmutableListMultimap.Builder<String, ColumnSchema> b = ImmutableListMultimap.builder();
        for (ColumnSchema cs : getColumnSchemas()) {
            b.put(cs.getName(), cs);
        }
        return b.build();
    }
    
    @Value.Check
    protected void check() {
        // Check indexes on column schemas.
        for (int i = 0; i < size(); ++i) {
            ColumnSchema cs = getColumnSchema(i);
            Preconditions.checkState(
                    cs.getIndex() == i,
                    "Column '%s' has incorrect index (was %d but expected %d).",
                    cs.getName(),
                    cs.getIndex(),
                    i);
        }
    }
}
