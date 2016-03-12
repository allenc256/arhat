package org.testdb.relation;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.immutables.value.Value;
import org.testdb.expression.Environment;
import org.testdb.expression.Expression;
import org.testdb.expression.Expressions;
import org.testdb.expression.aggregator.Aggregator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Value.Immutable
public abstract class AbstractGroupByRelation extends AbstractProjectionOrGroupByRelation {
    public abstract List<Expression> getGroupByExpressions();
    
    public abstract List<Aggregator<?, ?>> getAggregators();
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Stream<Tuple> getTupleStream() {
        List<Aggregator<?, ?>> as = getAggregators();
        Map<Tuple, List<Object>> accumulatorsByKey = Maps.newHashMap();
        
        try (Stream<Tuple> tuples  = getSourceRelation().getTupleStream()) {
            tuples.forEach(tuple -> {
                Tuple key = Expressions.evaluate(tuple, getGroupByExpressions());
                List<Object> values = accumulatorsByKey.get(key);
                
                if (values == null) {
                    values = Lists.newArrayListWithCapacity(as.size());
                    for (int i = 0; i < as.size(); ++i) {
                        values.add(as.get(i).emptyAccumulator());
                    }
                    accumulatorsByKey.put(key, values);
                }
                
                for (int i = 0; i < as.size(); ++i) {
                    values.set(i, ((Aggregator)as.get(i)).aggregate(tuple, values.get(i)));
                }
            });
        }
        
        
        return accumulatorsByKey.entrySet().stream().map(e -> {
            List<Object> accs = e.getValue();
            List<Object> values = accumulatorsToValues(as, accs);
            return Expressions.evaluate(
                    new AggregationEnvironment(e.getKey(), values),
                    getTargetExpressions());
        });
    }

    private List<Object> accumulatorsToValues(List<Aggregator<?, ?>> aggregators,
                                              List<Object> accumulators) {
        return new AbstractList<Object>() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Object get(int index) {
                return ((Aggregator)aggregators.get(index)).getValue(accumulators.get(index));
            }

            @Override
            public int size() {
                return accumulators.size();
            }
        };
    }
    
    private static class AggregationEnvironment implements Environment {
        private final Tuple tuple;
        private final List<Object> aggregationValues;
        
        private AggregationEnvironment(Tuple tuple,
                                       List<Object> aggregationValues) {
            this.tuple = tuple;
            this.aggregationValues = aggregationValues;
        }

        @Override
        public Object getAggregationValue(int index) {
            return aggregationValues.get(index);
        }

        @Override
        public Object get(int index) {
            return tuple.get(index);
        }
    }
}
