package org.testdb.expression;

import java.util.Collection;

import org.immutables.value.Value;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.QualifiedName;
import org.testdb.relation.TupleSchema;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

@Value.Immutable
public abstract class AbstractVariableExpression implements Expression {
    public abstract int getVariableIndex();
    
    public abstract Optional<String> getName();

    @Override
    public Object evaluate(Environment env) {
        return env.get(getVariableIndex());
    }
    
    public static Expression forIdentifier(TupleSchema tupleSchema,
                                           QualifiedName columnName) {
        Collection<ColumnSchema> css = tupleSchema.getColumnSchemas(columnName);
        
        Preconditions.checkState(
                !css.isEmpty(),
                "Column '%s' does not exist.",
                columnName);
        Preconditions.checkState(
                css.size() <= 1,
                "Column name '%s' is ambiguous.",
                columnName);

        ColumnSchema cs = Iterables.getOnlyElement(css);
        return ImmutableVariableExpression.builder()
                .variableIndex(cs.getIndex())
                .type(cs.getType())
                .name(cs.getName())
                .build();
    }
}
