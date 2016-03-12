package org.testdb.expression;

import org.immutables.value.Value;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.Tuple;
import org.testdb.relation.TupleSchema;
import org.testdb.type.SqlType;

import com.google.common.collect.Iterables;

@Value.Immutable
public abstract class AbstractIdentifierExpression implements Expression {
    public abstract TupleSchema getTupleSchema();
    
    public abstract String getColumnName();
    
    @Value.Derived
    public int getColumnIndex() {
        Iterable<ColumnSchema> css = getTupleSchema().getColumnSchemas(getColumnName());
        if (Iterables.size(css) != 1) {
            throw new IllegalStateException(String.format(
                    "Column name '%s' is ambiguous.",
                    getColumnName()));
        }
        return Iterables.getOnlyElement(css).getIndex();
    }
    
    @Override
    public SqlType getType() {
        return getTupleSchema().getColumnSchema(getColumnIndex()).getType();
    }

    @Override
    public Object evaluate(Tuple tuple) {
        return tuple.get(getColumnIndex());
    }
}
