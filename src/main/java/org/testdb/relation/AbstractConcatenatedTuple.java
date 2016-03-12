package org.testdb.relation;

import java.util.AbstractList;
import java.util.List;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractConcatenatedTuple implements Tuple {
    public abstract Tuple getLeftTuple();
    public abstract Tuple getRightTuple();
    
    @Override
    public List<Object> getValues() {
        return new AbstractList<Object>() {
            @Override
            public Object get(int index) {
                if (index < getLeftTuple().size()) {
                    return getLeftTuple().get(index);
                } else {
                    return getRightTuple().get(index - getLeftTuple().size());
                }
            }

            @Override
            public int size() {
                return getLeftTuple().size() + getRightTuple().size();
            }
        };
    }
}
