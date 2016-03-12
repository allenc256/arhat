package org.testdb.expression;

public class UnaryOperators {
    private UnaryOperators() {
        // empty
    }
    
    public static final UnaryOperator<Object, Boolean> IS_NULL = value -> value == null;
    public static final UnaryOperator<Object, Boolean> IS_NOT_NULL = value -> value != null;
    
    public static final UnaryOperator<Boolean, Boolean> NOT = value -> value != null ? !value : null;
    
    public static final UnaryOperator<Integer, Integer> NEGATE_INTEGER = value -> value != null ? -value : null;
}
