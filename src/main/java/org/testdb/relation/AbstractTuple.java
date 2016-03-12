package org.testdb.relation;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.google.common.base.Preconditions;

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
    }
}
