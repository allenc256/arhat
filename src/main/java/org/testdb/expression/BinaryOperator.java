package org.testdb.expression;

public interface BinaryOperator<T, R> {
    R apply(T left, T right);
}
