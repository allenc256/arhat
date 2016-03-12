package org.testdb.relation;

import org.immutables.value.Value;
import org.testdb.parse.SqlStrings;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractColumnSchema implements ColumnSchema {
    @Value.Check
    protected void check() {
        Preconditions.checkState(
                getQualifierAliases().stream().allMatch(SqlStrings::isLowerCase),
                "Qualifier aliases must be lower-case.");
        Preconditions.checkState(
                SqlStrings.isLowerCase(getName()),
                "Column name must be lower-case.");
    }
}
