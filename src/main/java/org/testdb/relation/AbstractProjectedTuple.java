package org.testdb.relation;

import java.util.AbstractList;
import java.util.List;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(jdkOnly = true)
public abstract class AbstractProjectedTuple implements Tuple {
    public abstract Tuple getSourceTuple();

    /**
     * Describes how the source schema should map to the target schema.
     * <p>
     * The ith index of this list describes what column from the source schema
     * should map to the ith column of the target schema. Specifically, it
     * should contain a column index from the source schema.
     */
    public abstract List<Integer> getSchemaMapping();

    @Override
    public List<Object> getValues() {
        return new AbstractList<Object>() {
            @Override
            public Object get(int index) {
                return getSourceTuple().get(getSchemaMapping().get(index));
            }

            @Override
            public int size() {
                return getSchemaMapping().size();
            }
        };
    }
}
