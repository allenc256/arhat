package org.testdb.expression;

import com.google.common.base.Optional;

public interface NamedExpression extends Expression {
    /**
     * Name used as column name if this expression is a target expression in a
     * select statement.
     * <p>
     * For variable expressions, this is the name of source column (e.g. "foo"
     * in "SELECT foo FROM bar"). For aggregations, this is the name of the
     * aggregation in lowercase (e.g., "sum" IN "SELECT SUM(foo) FROM bar").
     */
    Optional<String> getName();
}
