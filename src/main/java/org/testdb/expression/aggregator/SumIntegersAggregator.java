package org.testdb.expression.aggregator;

import org.testdb.expression.Expression;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;

/**
 * Notes on semantics here:
 * <ul>
 * <li>If the set of input values is empty, the sum is NULL.</li>
 * <li>If the set of input values are all NULL, the sum is NULL.</li>
 * <li>Otherwise, the sum is the sum of all non-null input integers.</li>
 * </ul>
 */
public class SumIntegersAggregator extends ExpressionAggregator<Integer, Integer, Integer> {
    public SumIntegersAggregator(Expression inputExpression) {
        super(inputExpression);
        
        Preconditions.checkArgument(
                inputExpression.getType() == SqlType.INTEGER,
                "Expression to be summed must be integer-typed.");
    }

    @Override
    protected Integer aggregate(Integer value, Integer sum) {
        if (value == null) {
            return sum;
        } else {
            return sum != null ? Math.addExact(sum, value) : value;
        }
    }

    @Override
    public Integer getValue(Integer accumulator) {
        return accumulator;
    }

    @Override
    public SqlType getType() {
        return SqlType.INTEGER;
    }
}
