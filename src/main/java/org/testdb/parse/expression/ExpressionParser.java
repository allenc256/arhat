package org.testdb.parse.expression;

import java.util.Collection;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.testdb.expression.Expression;
import org.testdb.expression.ImmutableAggregationVariableExpression;
import org.testdb.expression.ImmutableLiteralExpression;
import org.testdb.expression.ImmutableUnaryExpression;
import org.testdb.expression.ImmutableVariableExpression;
import org.testdb.expression.UnaryOperators;
import org.testdb.expression.aggregator.Aggregator;
import org.testdb.expression.aggregator.CountAggregator;
import org.testdb.expression.aggregator.CountDistinctAggregator;
import org.testdb.expression.aggregator.SumIntegersAggregator;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.ExpressionAggregateContext;
import org.testdb.parse.SQLParser.ExpressionAndOrContext;
import org.testdb.parse.SQLParser.ExpressionCompareContext;
import org.testdb.parse.SQLParser.ExpressionConcatContext;
import org.testdb.parse.SQLParser.ExpressionCountDistinctContext;
import org.testdb.parse.SQLParser.ExpressionCountStarContext;
import org.testdb.parse.SQLParser.ExpressionIdContext;
import org.testdb.parse.SQLParser.ExpressionIsNotNullContext;
import org.testdb.parse.SQLParser.ExpressionIsNullContext;
import org.testdb.parse.SQLParser.ExpressionLiteralContext;
import org.testdb.parse.SQLParser.ExpressionMultDivContext;
import org.testdb.parse.SQLParser.ExpressionNegateContext;
import org.testdb.parse.SQLParser.ExpressionNotContext;
import org.testdb.parse.SQLParser.ExpressionParensContext;
import org.testdb.parse.SQLParser.ExpressionPlusMinusContext;
import org.testdb.parse.SqlParseException;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableQualifiedName;
import org.testdb.relation.QualifiedName;
import org.testdb.relation.TupleSchema;
import org.testdb.type.SqlType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ExpressionParser extends SQLBaseVisitor<Expression> {
    private final TupleSchema tupleSchema;
    private final Optional<TupleSchema> partitionTupleSchema;
    private final List<Aggregator<?, ?>> aggregators;
    
    public ExpressionParser(TupleSchema tupleSchema,
                            Optional<TupleSchema> partitionTupleSchema,
                            List<Aggregator<?, ?>> aggregators) {
        this.tupleSchema = tupleSchema;
        this.partitionTupleSchema = partitionTupleSchema;
        this.aggregators = aggregators;
    }
    
    public ExpressionParser(TupleSchema tupleSchema) {
        this(tupleSchema, Optional.absent(), ImmutableList.of());
    }
    
    public List<Aggregator<?, ?>> getAggregators() {
        return aggregators;
    }

    @Override
    public Expression visitExpressionLiteral(ExpressionLiteralContext ctx) {
        LiteralParser visitor = new LiteralParser();
        ctx.literal().accept(visitor);
        if (visitor.getType() == null) {
            throw SqlParseException.create(
                    ctx.getStart(),
                    "failed to parse literal.");
        }
        return ImmutableLiteralExpression.builder()
                .value(visitor.getValue())
                .type(visitor.getType())
                .build();
    }

    @Override
    public Expression visitExpressionId(ExpressionIdContext ctx) {
        QualifiedName columnName;
        Token columnNameToken;
        
        if (ctx.ID().size() == 1) {
            columnName = ImmutableQualifiedName.of(ctx.ID(0).getText());
            columnNameToken = ctx.ID(0).getSymbol();
        } else if (ctx.ID().size() == 2) {
            columnName = ImmutableQualifiedName.of(ctx.ID(0).getText(), ctx.ID(1).getText());
            columnNameToken = ctx.ID(1).getSymbol();
        } else {
            throw SqlParseException.create(
                    ctx.ID().get(0).getSymbol(),
                    "failed to parse identifier expression (unexpected number of identifiers).");
        }

        // Do some validation so that we can print a nicer error to the user.
        Collection<ColumnSchema> css = tupleSchema.getColumnSchemas(columnName);
        if (css.isEmpty()) {
            throw SqlParseException.create(
                    columnNameToken,
                    "column '%s' does not exist.",
                    columnName);
        }
        if (css.size() > 1) {
            throw SqlParseException.create(
                    columnNameToken,
                    "column '%s' is ambiguous.",
                    columnName);
        }
        
        return ImmutableVariableExpression.forIdentifier(tupleSchema, columnName);
    }

    @Override
    public Expression visitExpressionIsNotNull(ExpressionIsNotNullContext ctx) {
        return ImmutableUnaryExpression.builder()
                .inputExpression(ctx.expression().accept(this))
                .operator(UnaryOperators.IS_NOT_NULL)
                .type(SqlType.BOOLEAN)
                .build();
    }
    
    @Override
    public Expression visitExpressionIsNull(ExpressionIsNullContext ctx) {
        return ImmutableUnaryExpression.builder()
                .inputExpression(ctx.expression().accept(this))
                .operator(UnaryOperators.IS_NULL)
                .type(SqlType.BOOLEAN)
                .build();
    }

    @Override
    public Expression visitExpressionNegate(ExpressionNegateContext ctx) {
        return UnaryOperatorExpressionParser.parse(
                ctx.expression().accept(this),
                ctx.MINUS_SYMBOL().getSymbol());
    }

    @Override
    public Expression visitExpressionNot(ExpressionNotContext ctx) {
        return UnaryOperatorExpressionParser.parse(
                ctx.expression().accept(this),
                ctx.NOT().getSymbol());
    }

    @Override
    public Expression visitExpressionConcat(ExpressionConcatContext ctx) {
        return BinaryOperatorExpressionParser.parse(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.CONCAT_SYMBOL().getSymbol());
    }

    @Override
    public Expression visitExpressionCompare(ExpressionCompareContext ctx) {
        return BinaryOperatorExpressionParser.parse(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    @Override
    public Expression visitExpressionMultDiv(ExpressionMultDivContext ctx) {
        return BinaryOperatorExpressionParser.parse(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    @Override
    public Expression visitExpressionPlusMinus(ExpressionPlusMinusContext ctx) {
        return BinaryOperatorExpressionParser.parse(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    @Override
    public Expression visitExpressionAndOr(ExpressionAndOrContext ctx) {
        return BinaryOperatorExpressionParser.parse(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.op);
    }

    @Override
    public Expression visitExpressionParens(ExpressionParensContext ctx) {
        return ctx.expression().accept(this);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Aggregators
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Expression visitExpressionAggregate(ExpressionAggregateContext ctx) {
        checkAggregationsAllowed(ctx.fn);
        
        ExpressionParser inputExpressionParser = new ExpressionParser(
                partitionTupleSchema.get());
        Expression inputExpression = ctx.expression().accept(
                inputExpressionParser);

        return constructExpression(
                ctx.fn.getText(),
                parseAggregator(ctx.fn, inputExpression));
    }

    private Aggregator<?, ?> parseAggregator(Token aggregationFunToken,
                                             Expression inputExpression) {
        switch (aggregationFunToken.getType()) {
        case SQLParser.SUM:
            return new SumIntegersAggregator(inputExpression);
        case SQLParser.COUNT:
            return new CountAggregator();
        default:
            throw SqlParseException.create(
                    aggregationFunToken,
                    "Unsupported aggregation function.");
        }
    }

    @Override
    public Expression visitExpressionCountStar(ExpressionCountStarContext ctx) {
        checkAggregationsAllowed(ctx.COUNT().getSymbol());
        
        return constructExpression("count", new CountAggregator());
    }

    @Override
    public Expression visitExpressionCountDistinct(ExpressionCountDistinctContext ctx) {
        checkAggregationsAllowed(ctx.COUNT().getSymbol());
        
        ExpressionParser inputExpressionParser = new ExpressionParser(
                partitionTupleSchema.get());
        Expression inputExpression = ctx.expression().accept(
                inputExpressionParser);
        
        return constructExpression("count", new CountDistinctAggregator(inputExpression));
    }

    private void checkAggregationsAllowed(Token token) {
        if (!partitionTupleSchema.isPresent()) {
            throw SqlParseException.create(
                    token,
                    "Cannot specify aggregation function in this context.");
        }
    }

    private Expression constructExpression(String name, Aggregator<?, ?> aggregator) {
        int varIndex = aggregators.size();
        
        aggregators.add(aggregator);
        
        return ImmutableAggregationVariableExpression.builder()
                .variableIndex(varIndex)
                .name(name)
                .type(aggregator.getType())
                .build();
    }
}
