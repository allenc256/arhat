package org.testdb.expression.aggregator;

import org.testdb.relation.Tuple;
import org.testdb.type.SqlType;

public class CountAggregator implements Aggregator<Long, Integer> {
    @Override
    public Long aggregate(Tuple tuple, Long accumulator) {
        return Math.addExact(accumulator, 1);
    }

    @Override
    public Long emptyAccumulator() {
        return 0L;
    }

    @Override
    public Integer getValue(Long accumulator) {
        return Math.toIntExact(accumulator);
    }

    @Override
    public SqlType getType() {
        return SqlType.INTEGER;
    }
}
