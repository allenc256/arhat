package org.testdb.expression;

import java.util.List;

import org.testdb.relation.ImmutableTuple;
import org.testdb.relation.Tuple;

import com.google.common.collect.Lists;

public class Expressions {
    private Expressions() {
        // empty
    }
    
    public static Tuple evaluate(Environment env, List<Expression> expressions) {
        List<Object> values = Lists.newArrayListWithCapacity(
                expressions.size());
        
        for (Expression expression : expressions) {
            values.add(expression.evaluate(env));
        }
        
        return ImmutableTuple.builder().values(values).build();
    }
}
