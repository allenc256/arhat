package org.testdb.relation;

import java.util.Set;

import org.testdb.type.SqlType;

import com.google.common.base.Optional;

public interface ColumnSchema {
    int getIndex();
    
    /**
     * Set of valid qualifier aliases which may be used to refer to this column.
     * These are typically the source relation/table name and any user-specified
     * alias. This set can be empty (e.g., when selecting from an anonymous
     * sub-select statement).
     * <p>
     * For example, valid qualifier aliases for "SELECT * FROM foo f" would be
     * "foo" and "f".
     */
    Set<String> getQualifierAliases();

    /**
     * The name of the column. This is optional, as its possible for columns to
     * be anonymous (e.g., a derived column without an explicitly set alias).
     */
    Optional<String> getName();
    
    SqlType getType();
}
