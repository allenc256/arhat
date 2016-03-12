package org.testdb.expression;

import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser.ExpressionEqContext;
import org.testdb.parse.SQLParser.ExpressionIdContext;
import org.testdb.parse.SQLParser.ExpressionLiteralContext;
import org.testdb.parse.SQLParser.ExpressionParensContext;
import org.testdb.relation.TupleSchema;

public class ExpressionVisitor extends SQLBaseVisitor<Expression<? extends Object>> {
    private final TupleSchema tupleSchema;
    
    public ExpressionVisitor(TupleSchema tupleSchema) {
        this.tupleSchema = tupleSchema;
    }

    @Override
    public Expression<? extends Object> visitExpressionLiteral(ExpressionLiteralContext ctx) {
        return ImmutableLiteralExpression.builder()
                .value(ctx.literal().accept(LiteralVisitor.instance()))
                .build();
    }

    @Override
    public Expression<? extends Object> visitExpressionId(ExpressionIdContext ctx) {
        return ImmutableIdentifierExpression.builder()
                .tupleSchema(tupleSchema)
                .columnName(ctx.ID().getText())
                .build();
    }

    @Override
    public Expression<? extends Object> visitExpressionEq(ExpressionEqContext ctx) {
        return ImmutableEqualExpression.builder()
                .leftExpression(ctx.expression(0).accept(this))
                .rightExpression(ctx.expression(1).accept(this))
                .build();
    }

    @Override
    public Expression<? extends Object> visitExpressionParens(ExpressionParensContext ctx) {
        return ctx.expression().accept(this);
    }
}
