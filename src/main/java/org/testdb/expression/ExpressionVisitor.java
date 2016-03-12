package org.testdb.expression;

import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.Token;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.ExpressionAndOrContext;
import org.testdb.parse.SQLParser.ExpressionCompareContext;
import org.testdb.parse.SQLParser.ExpressionConcatContext;
import org.testdb.parse.SQLParser.ExpressionIdContext;
import org.testdb.parse.SQLParser.ExpressionLiteralContext;
import org.testdb.parse.SQLParser.ExpressionMultDivContext;
import org.testdb.parse.SQLParser.ExpressionNotContext;
import org.testdb.parse.SQLParser.ExpressionParensContext;
import org.testdb.parse.SQLParser.ExpressionPlusMinusContext;
import org.testdb.relation.ImmutableQualifiedName;
import org.testdb.relation.QualifiedName;
import org.testdb.relation.TupleSchema;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ExpressionVisitor extends SQLBaseVisitor<Expression> {
    private final TupleSchema tupleSchema;
    
    public ExpressionVisitor(TupleSchema tupleSchema) {
        this.tupleSchema = tupleSchema;
    }

    @Override
    public Expression visitExpressionLiteral(ExpressionLiteralContext ctx) {
        LiteralVisitor visitor = new LiteralVisitor();
        ctx.literal().accept(visitor);
        Preconditions.checkState(
                visitor.getType() != null,
                "Failed to properly parse literal.");
        return ImmutableLiteralExpression.builder()
                .value(visitor.getValue())
                .type(visitor.getType())
                .build();
    }

    @Override
    public Expression visitExpressionId(ExpressionIdContext ctx) {
        QualifiedName columnName;
        if (ctx.ID().size() == 1) {
            columnName = ImmutableQualifiedName.of(ctx.ID(0).getText());
        } else if (ctx.ID().size() == 2) {
            columnName = ImmutableQualifiedName.of(ctx.ID(0).getText(), ctx.ID(1).getText());
        } else {
            throw new IllegalStateException("Failed to parse identifier expression "
                    + "(unexpected number of identifiers).");
        }
        return ImmutableIdentifierExpression.builder()
                .tupleSchema(tupleSchema)
                .columnName(columnName)
                .build();
    }
    
    @Override
    public Expression visitExpressionNot(ExpressionNotContext ctx) {
        return ImmutableNotExpression.builder()
                .sourceExpression(ctx.expression().accept(this))
                .build();
    }

    @Override
    public Expression visitExpressionConcat(ExpressionConcatContext ctx) {
        return parseExpressionBinary(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.CONCAT_SYMBOL().getSymbol());
    }

    @Override
    public Expression visitExpressionCompare(ExpressionCompareContext ctx) {
        return parseExpressionBinary(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    @Override
    public Expression visitExpressionMultDiv(ExpressionMultDivContext ctx) {
        return parseExpressionBinary(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    @Override
    public Expression visitExpressionPlusMinus(ExpressionPlusMinusContext ctx) {
        return parseExpressionBinary(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    @Override
    public Expression visitExpressionAndOr(ExpressionAndOrContext ctx) {
        return parseExpressionBinary(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    private Expression parseExpressionBinary(Expression left,
                                             Expression right,
                                             Token opToken) {
        SqlType inputType = !left.getType().equals(SqlType.NULL) ? left.getType() : right.getType();
        SqlType resultType = parseExpressionBinaryResultType(inputType, opToken);
        BinaryOperator<?, ?> operator = parseExpressionBinaryOperator(inputType, opToken);
        return ImmutableBinaryExpression.builder()
                .leftExpression(left)
                .rightExpression(right)
                .operator(operator)
                .type(resultType)
                .build();
    }
    
    private SqlType parseExpressionBinaryResultType(SqlType inputType,
                                                    Token opToken) {
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
    
    private static final Map<Entry<SqlType, Integer>, BinaryOperator<?, ?>> BINARY_OPERATOR_TABLE;
    
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
        
        BINARY_OPERATOR_TABLE = b.build();
    }
    
    private static void register(ImmutableMap.Builder<Entry<SqlType, Integer>, BinaryOperator<?, ?>> b,
                                 SqlType sqlType,
                                 int opToken,
                                 BinaryOperator<?, ?> op) {
        b.put(Maps.immutableEntry(sqlType, opToken), op);
    }
    
    private BinaryOperator<?, ?> parseExpressionBinaryOperator(SqlType inputType, Token opToken) {
        // N.B., the equals operator is always the same regardless of input type.
        if (opToken.getType() == SQLParser.EQ_SYMBOL) {
            return BinaryOperators.EQUALS;
        }
        
        BinaryOperator<?, ?> op = BINARY_OPERATOR_TABLE.get(
                Maps.immutableEntry(inputType, opToken.getType()));
        if (op == null) {
            throw new UnsupportedOperationException(String.format(
                    "Do not know how to parse operator '%s' against input type %s.",
                    opToken.getText(),
                    inputType));
        }
        return op;
    }

    @Override
    public Expression visitExpressionParens(ExpressionParensContext ctx) {
        return ctx.expression().accept(this);
        
    }
}
