package org.testdb.expression;

import java.util.Collection;

import org.immutables.value.Value;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.QualifiedName;
import org.testdb.relation.Tuple;
import org.testdb.relation.TupleSchema;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

@Value.Immutable
public abstract class AbstractIdentifierExpression implements Expression {
    public abstract TupleSchema getTupleSchema();
    
    public abstract QualifiedName getColumnName();
    
    @Value.Derived
    public int getColumnIndex() {
        Collection<ColumnSchema> css = getTupleSchema().getColumnSchemas(getColumnName());
        
        Preconditions.checkState(
                !css.isEmpty(),
                "Column '%s' does not exist.",
                getColumnName());
        Preconditions.checkState(
                css.size() <= 1,
                "Column name '%s' is ambiguous.",
                getColumnName());

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
