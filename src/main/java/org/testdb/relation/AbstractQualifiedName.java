package org.testdb.relation;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractQualifiedName implements QualifiedName {
    public static QualifiedName of(String name) {
        return ImmutableQualifiedName.builder().name(name).build();
    }
    
    public static QualifiedName of(String qualifier, String name) {
        return ImmutableQualifiedName.builder().qualifier(qualifier).name(name).build();
    }
    
    @Override
    public String toString() {
        if (getQualifier().isPresent()) {
            return getQualifier().get() + "." + getName();
        } else {
            return getName();
        }
    }
}
