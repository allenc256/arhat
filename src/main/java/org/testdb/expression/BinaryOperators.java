package org.testdb.expression;

public class BinaryOperators {
    private BinaryOperators() {
        // empty
    }
    
    public static final BinaryOperator<Object, Boolean> EQUALS = (left, right) -> {
        return left == null || right == null ? false : left.equals(right);
    };
    
    public static final BinaryOperator<Integer, Boolean> LT_INTEGERS = (left, right) -> {
        return left == null || right == null ? false : left < right;
    };
    public static final BinaryOperator<Integer, Boolean> LTE_INTEGERS = (left, right) -> {
        return left == null || right == null ? false : left <= right;
    };
    public static final BinaryOperator<Integer, Boolean> GT_INTEGERS = (left, right) -> {
        return left == null || right == null ? false : left > right;
    };
    public static final BinaryOperator<Integer, Boolean> GTE_INTEGERS = (left, right) -> {
        return left == null || right == null ? false : left >= right;
    };
    
    public static final BinaryOperator<String, Boolean> LT_STRINGS = (left, right) -> {
        return left == null || right == null ? false : left.compareTo(right) < 0;
    };
    public static final BinaryOperator<String, Boolean> LTE_STRINGS = (left, right) -> {
        return left == null || right == null ? false : left.compareTo(right) <= 0;
    };
    public static final BinaryOperator<String, Boolean> GT_STRINGS = (left, right) -> {
        return left == null || right == null ? false : left.compareTo(right) > 0;
    };
    public static final BinaryOperator<String, Boolean> GTE_STRINGS = (left, right) -> {
        return left == null || right == null ? false : left.compareTo(right) >= 0;
    };
    public static final BinaryOperator<String, String> CONCAT_STRINGS = (left, right) -> {
        return left == null || right == null ? null : left + right;
    };

    public static final BinaryOperator<Integer, Integer> MULTIPLY_INTEGERS = (left, right) -> {
        return left == null || right == null ? null : left * right;
    };
    public static final BinaryOperator<Integer, Integer> DIVIDE_INTEGERS = (left, right) -> {
        return left == null || right == null ? null : left / right;
    };
    public static final BinaryOperator<Integer, Integer> ADD_INTEGERS = (left, right) -> {
        return left == null || right == null ? null : left + right;
    };
    public static final BinaryOperator<Integer, Integer> SUBTRACT_INTEGERS = (left, right) -> {
        return left == null || right == null ? null : left - right;
    };
    
    public static final BinaryOperator<Boolean, Boolean> AND = (left, right) -> {
        return left == null || right == null ? null : left && right;
    };
    public static final BinaryOperator<Boolean, Boolean> OR = (left, right) -> {
        return left == null || right == null ? null : left || right;
    };
    
    public static final BinaryOperator<Void, Void> NULL = (left, right) -> {
        return null;
    };
}
