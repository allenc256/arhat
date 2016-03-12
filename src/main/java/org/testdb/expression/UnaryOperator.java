package org.testdb.expression;

public interface UnaryOperator<T, R> {
    R apply(T value);
}
