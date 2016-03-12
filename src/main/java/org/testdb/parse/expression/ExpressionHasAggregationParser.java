package org.testdb.parse.expression;

import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser.ExpressionAggregateContext;
import org.testdb.parse.SQLParser.ExpressionCountDistinctContext;
import org.testdb.parse.SQLParser.ExpressionCountStarContext;

public class ExpressionHasAggregationParser extends SQLBaseVisitor<Boolean> {
    @Override
    public Boolean visitExpressionAggregate(ExpressionAggregateContext ctx) {
        return true;
    }

    @Override
    public Boolean visitExpressionCountStar(ExpressionCountStarContext ctx) {
        return true;
    }

    @Override
    public Boolean visitExpressionCountDistinct(ExpressionCountDistinctContext ctx) {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        if (aggregate == null) {
            aggregate = false;
        }
        if (nextResult == null) {
            nextResult = false;
        }
        return aggregate || nextResult;
    }
}
