package org.testdb.relation;

import java.util.List;

import org.immutables.value.Value;
import org.testdb.expression.Expression;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;

abstract class AbstractProjectionOrGroupByRelation implements Relation {
    public abstract List<Expression> getTargetExpressions();
    
    public abstract Relation getSourceRelation();

    @Value.Check
    protected void check() {
        TupleSchema targetSchema = getTupleSchema();
        
        Preconditions.checkState(
                getTargetExpressions().size() == targetSchema.size(),
                "Size of mapping must match size of target schema.");
        
        for (int i = 0; i < getTargetExpressions().size(); ++i) {
            SqlType schemaType = getTupleSchema().getColumnSchema(i).getType();
            SqlType expressionType = getTargetExpressions().get(i).getType();
            Preconditions.checkState(
                    schemaType.equals(expressionType),
                    "Schema type must match expression type (schema type %s, expression type %s).",
                    schemaType,
                    expressionType);
        }
    }
}
