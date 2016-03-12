package org.testdb.relation;

import java.util.List;

import org.immutables.value.Value;

import com.google.common.base.Preconditions;
import com.sun.istack.internal.Nullable;

@Value.Immutable
public abstract class AbstractTuple implements Tuple {
    // N.B., this is marked @Nullable so that values so that null values aren't
    // checked by generated builders.
    @Nullable
    @Override
    public abstract List<Object> getValues();

    @Value.Check
    protected void check() {
        Preconditions.checkNotNull(getValues());
        Preconditions.checkState(
                getValues().size() == getSchema().size(),
                "Mismatch between size of values and size of schema.");

        // Type-check tuple values.
        for (int i = 0; i < size(); ++i) {
            if (get(i) == null) {
                continue;
            }
            
            Class<?> expectedType = getSchema().getColumnSchema(i).getType();
            Class<? extends Object> actualType = get(i).getClass();
            if (!expectedType.isAssignableFrom(actualType)) {
                throw new IllegalStateException(
                        String.format(
                                "Column %d of tuple has invalid type (expected %s, was %s).",
                                i,
                                expectedType,
                                actualType));
            }
        }
    }
}
