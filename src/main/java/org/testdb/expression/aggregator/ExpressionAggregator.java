package org.testdb.expression.aggregator;

import org.testdb.expression.Expression;
import org.testdb.relation.Tuple;

abstract class ExpressionAggregator<T, A, V> implements Aggregator<A, V> {
    private final Expression inputExpression;

    public ExpressionAggregator(Expression inputExpression) {
        this.inputExpression = inputExpression;
    }
    
    protected abstract A aggregate(T value, A accumulator);

    @SuppressWarnings("unchecked")
    @Override
    public A aggregate(Tuple tuple, A accumulator) {
        return aggregate((T)inputExpression.evaluate(tuple), accumulator);
    }
}
