package org.testdb.expression;

/**
 * An environment contains a collection of variables (keyed by integer index)
 * that are used when evaluating an expression. There are two types of variables
 * that are supported:
 * <ul>
 * <li>"Normal" (or "tuple") variables, which simply reference a value within a
 * tuple that is being processed.</li>
 * <li>"Aggregation" variables, which reference the value of an aggregation
 * within a grouping partition that is being evaluated.</li>
 * </ul>
 */
public interface Environment {
    Object get(int index);
    
    default Object getAggregationValue(int index) {
        throw new UnsupportedOperationException();
    }
}
