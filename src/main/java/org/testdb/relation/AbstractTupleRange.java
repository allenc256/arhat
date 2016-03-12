package org.testdb.relation;

import org.immutables.value.Value;
import org.immutables.value.Value.Check;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractTupleRange implements TupleRange {
    @Check
    protected void check() {
        if (getLowerBound().isPresent()) {
            Preconditions.checkState(
                    getTupleSchema().equals(getLowerBound().get().getSchema()),
                    "Schema mismatch with lower-bound.");
        }
        
        if (getUpperBound().isPresent()) {
            Preconditions.checkState(
                    getTupleSchema().equals(getLowerBound().get().getSchema()),
                    "Schema mismatch with upper-bound.");
        }
        
        if (getLowerBound().isPresent() && getUpperBound().isPresent()) {
            boolean cmp = LexicographicTupleOrdering.INSTANCE.compare(
                    getLowerBound().get(),
                    getUpperBound().get()) <= 0;
            Preconditions.checkState(
                    cmp,
                    "Lower-bound must be less than or equal to upper-bound.");
        }
    }
}
