package org.testdb.relation;

import java.util.List;

import org.immutables.value.Value;
import org.testdb.expression.Expression;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@Value.Immutable
public abstract class AbstractProjectedRelation implements Relation {
    public abstract List<Expression> getExpressions();
    
    public abstract Relation getSourceRelation();

    @Override
    public Cursor<Tuple> getTuples() {
        Cursor<Tuple> tuples = getSourceRelation().getTuples();
        return new ForwardingCursor<Tuple>() {
            @Override
            protected Cursor<Tuple> delegate() {
                return tuples;
            }

            @Override
            public Tuple next() {
                Tuple source = delegate().next();
                List<Object> values = Lists.newArrayListWithCapacity(
                        getTupleSchema().size());
                
                for (Expression expression : getExpressions()) {
                    values.add(expression.evaluate(source));
                }
                
                return ImmutableTuple.builder().values(values).build();
            }
        };
    }

    @Value.Check
    protected void check() {
        TupleSchema targetSchema = getTupleSchema();
        
        Preconditions.checkState(
                getExpressions().size() == targetSchema.size(),
                "Size of mapping must match size of target schema.");
        
        for (int i = 0; i < getExpressions().size(); ++i) {
            SqlType schemaType = getTupleSchema().getColumnSchema(i).getType();
            SqlType expressionType = getExpressions().get(i).getType();
            Preconditions.checkState(
                    schemaType.equals(expressionType),
                    "Schema type must match expression type (schema type %s, expression type %s).",
                    schemaType,
                    expressionType);
        }
    }
}
