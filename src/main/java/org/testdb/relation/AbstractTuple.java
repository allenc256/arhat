package org.testdb.relation;

import org.immutables.value.Value;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractTuple implements Tuple {
    @Value.Check
    protected void check() {
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
                                "Column %d of tuple has invalid type (expected %s, was %d).",
                                i,
                                expectedType,
                                actualType));
            }
        }
    }
}
