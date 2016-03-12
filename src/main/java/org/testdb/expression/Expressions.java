package org.testdb.expression;

import java.util.List;

import org.testdb.relation.ImmutableTuple;
import org.testdb.relation.Tuple;
import org.testdb.relation.TupleSchema;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;
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
    
    public static void checkSchema(TupleSchema schema, List<Expression> targetExpressions) {
        Preconditions.checkState(
                targetExpressions.size() == schema.size(),
                "Size of mapping must match size of target schema.");
        
        for (int i = 0; i < targetExpressions.size(); ++i) {
            SqlType schemaType = schema.getColumnSchema(i).getType();
            SqlType expressionType = targetExpressions.get(i).getType();
            Preconditions.checkState(
                    schemaType.equals(expressionType),
                    "Schema type must match expression type (schema type %s, expression type %s).",
                    schemaType,
                    expressionType);
        }
    }
}
