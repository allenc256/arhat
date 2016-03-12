package org.testdb.relation;

import org.immutables.value.Value;
import org.testdb.parse.SqlStrings;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractQualifiedName implements QualifiedName {
    public static QualifiedName of(String name) {
        return ImmutableQualifiedName.builder()
                .name(name.toLowerCase())
                .build();
    }
    
    public static QualifiedName of(String qualifier, String name) {
        return ImmutableQualifiedName.builder()
                .qualifier(qualifier.toLowerCase())
                .name(name.toLowerCase())
                .build();
    }
    
    @Override
    public String toString() {
        if (getQualifier().isPresent()) {
            return getQualifier().get() + "." + getName();
        } else {
            return getName();
        }
    }

    @Value.Check
    protected void check() {
        Preconditions.checkState(
                SqlStrings.isLowerCase(getQualifier()),
                "Qualifier must be lower-case.");
        Preconditions.checkState(
                SqlStrings.isLowerCase(getName()),
                "Name must be lower-case.");
    }
}
