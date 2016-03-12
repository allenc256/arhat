package org.testdb.parse.expression;

import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.Token;
import org.testdb.expression.BinaryOperator;
import org.testdb.expression.BinaryOperators;
import org.testdb.expression.Expression;
import org.testdb.expression.ImmutableBinaryExpression;
import org.testdb.parse.SQLParser;
import org.testdb.type.SqlType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

class BinaryOperatorExpressionParser {
    private BinaryOperatorExpressionParser() {
        // empty
    }
    
    private static final Map<Entry<SqlType, Integer>, BinaryOperator<?, ?>> OPERATOR_TABLE;
    
    static {
        ImmutableMap.Builder<Entry<SqlType, Integer>, BinaryOperator<?, ?>> b = ImmutableMap.builder();
        
        register(b, SqlType.INTEGER, SQLParser.STAR_SYMBOL, BinaryOperators.MULTIPLY_INTEGERS);
        register(b, SqlType.INTEGER, SQLParser.DIV_SYMBOL, BinaryOperators.DIVIDE_INTEGERS);
        register(b, SqlType.INTEGER, SQLParser.PLUS_SYMBOL, BinaryOperators.ADD_INTEGERS);
        register(b, SqlType.INTEGER, SQLParser.MINUS_SYMBOL, BinaryOperators.SUBTRACT_INTEGERS);
        
        register(b, SqlType.INTEGER, SQLParser.LT_SYMBOL, BinaryOperators.LT_INTEGERS);
        register(b, SqlType.INTEGER, SQLParser.GT_SYMBOL, BinaryOperators.GT_INTEGERS);
        register(b, SqlType.INTEGER, SQLParser.LTE_SYMBOL, BinaryOperators.LTE_INTEGERS);
        register(b, SqlType.INTEGER, SQLParser.GTE_SYMBOL, BinaryOperators.GTE_INTEGERS);
        
        register(b, SqlType.STRING, SQLParser.CONCAT_SYMBOL, BinaryOperators.CONCAT_STRINGS);
        
        register(b, SqlType.STRING, SQLParser.LT_SYMBOL, BinaryOperators.LT_STRINGS);
        register(b, SqlType.STRING, SQLParser.GT_SYMBOL, BinaryOperators.GT_STRINGS);
        register(b, SqlType.STRING, SQLParser.LTE_SYMBOL, BinaryOperators.LTE_STRINGS);
        register(b, SqlType.STRING, SQLParser.GTE_SYMBOL, BinaryOperators.GTE_STRINGS);
        
        register(b, SqlType.BOOLEAN, SQLParser.AND, BinaryOperators.AND);
        register(b, SqlType.BOOLEAN, SQLParser.OR, BinaryOperators.OR);
        
        OPERATOR_TABLE = b.build();
    }
    
    private static void register(ImmutableMap.Builder<Entry<SqlType, Integer>, BinaryOperator<?, ?>> b,
                                 SqlType sqlType,
                                 int opToken,
                                 BinaryOperator<?, ?> op) {
        b.put(Maps.immutableEntry(sqlType, opToken), op);
    }
    
    public static Expression parse(Expression left,
                                   Expression right,
                                   Token opToken) {
        SqlType inputType = !left.getType().equals(SqlType.NULL) ? left.getType() : right.getType();
        SqlType resultType = parseResultType(inputType, opToken);
        BinaryOperator<?, ?> operator = parseOperator(inputType, opToken);
        return ImmutableBinaryExpression.builder()
                .leftExpression(left)
                .rightExpression(right)
                .operator(operator)
                .type(resultType)
                .build();
    }
    
    private static SqlType parseResultType(SqlType inputType, Token opToken) {
        switch (opToken.getType()) {
        case SQLParser.EQ_SYMBOL:
        case SQLParser.LT_SYMBOL:
        case SQLParser.GT_SYMBOL:
        case SQLParser.LTE_SYMBOL:
        case SQLParser.GTE_SYMBOL:
        case SQLParser.AND:
        case SQLParser.OR:
            return SqlType.BOOLEAN;
        case SQLParser.STAR_SYMBOL:
        case SQLParser.DIV_SYMBOL:
        case SQLParser.PLUS_SYMBOL:
        case SQLParser.MINUS_SYMBOL:
            return inputType;
        case SQLParser.CONCAT_SYMBOL:
            return SqlType.STRING;
        default:
            throw new UnsupportedOperationException(String.format(
                    "Unrecognized operator '%s'.", opToken.getText()));
        }
    }
    
    private static BinaryOperator<?, ?> parseOperator(SqlType inputType, Token opToken) {
        // N.B., the equals operator is always the same regardless of input type.
        if (opToken.getType() == SQLParser.EQ_SYMBOL) {
            return BinaryOperators.EQUALS;
        }
        
        BinaryOperator<?, ?> op = OPERATOR_TABLE.get(
                Maps.immutableEntry(inputType, opToken.getType()));
        if (op == null) {
            throw new UnsupportedOperationException(String.format(
                    "Do not know how to parse operator '%s' against input type %s.",
                    opToken.getText(),
                    inputType));
        }
        return op;
    }
}
