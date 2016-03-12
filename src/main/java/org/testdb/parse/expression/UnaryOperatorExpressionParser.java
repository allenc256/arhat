package org.testdb.parse.expression;

import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.Token;
import org.testdb.expression.Expression;
import org.testdb.expression.ImmutableUnaryExpression;
import org.testdb.expression.UnaryOperator;
import org.testdb.expression.UnaryOperators;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SqlParseException;
import org.testdb.type.SqlType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class UnaryOperatorExpressionParser {
    private UnaryOperatorExpressionParser() {
        // empty
    }
    
    private static final Map<Entry<SqlType, Integer>, UnaryOperator<?, ?>> OPERATOR_TABLE;
    
    static {
        ImmutableMap.Builder<Entry<SqlType, Integer>, UnaryOperator<?, ?>> b = ImmutableMap.builder();
        
        register(b, SqlType.INTEGER, SQLParser.MINUS_SYMBOL, UnaryOperators.NEGATE_INTEGER);
        register(b, SqlType.BOOLEAN, SQLParser.NOT, UnaryOperators.NOT);
        
        OPERATOR_TABLE = b.build();
    }
    
    private static void register(ImmutableMap.Builder<Entry<SqlType, Integer>, UnaryOperator<?, ?>> b,
                                 SqlType sqlType,
                                 int opToken,
                                 UnaryOperator<?, ?> op) {
        b.put(Maps.immutableEntry(sqlType, opToken), op);
    }
    
    public static Expression parse(Expression input, Token opToken) {
        SqlType resultType = parseResultType(input.getType(), opToken);
        UnaryOperator<?, ?> operator = parseOperator(input.getType(), opToken);
        return ImmutableUnaryExpression.builder()
                .inputExpression(input)
                .operator(operator)
                .type(resultType)
                .build();
    }
    
    private static SqlType parseResultType(SqlType inputType, Token opToken) {
        switch (opToken.getType()) {
        case SQLParser.NOT:
            return SqlType.BOOLEAN;
        case SQLParser.MINUS_SYMBOL:
            return inputType;
        default:
            throw SqlParseException.create(
                    opToken,
                    "unrecognized operator '%s'.",
                    opToken.getText());
        }
    }
    
    private static UnaryOperator<?, ?> parseOperator(SqlType inputType, Token opToken) {
        UnaryOperator<?, ?> op = OPERATOR_TABLE.get(
                Maps.immutableEntry(inputType, opToken.getType()));
        if (op == null) {
            throw SqlParseException.create(
                    opToken,
                    "do not know how to parse operator '%s' against input type %s.",
                    opToken.getText(),
                    inputType);
        }
        return op;
    }
}
