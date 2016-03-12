package org.testdb.relation;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.immutables.value.Value;

// N.B., this wrapper should work even when no name is specified ---
// this is useful for removing all qualifiers from the schema of the
// relation. For example, this is used in evaluating "anonymous" sub-selects.
@Value.Immutable
public abstract class AbstractNamedRelation implements Relation {
    public abstract Relation getSourceRelation();
    
    public abstract Set<String> getAliases();
    
    @Value.Derived
    @Override
    public TupleSchema getTupleSchema() {
        ImmutableTupleSchema.Builder builder = ImmutableTupleSchema.builder()
                .from(getSourceRelation().getTupleSchema());
        
        builder.columnSchemas(getSourceRelation()
                .getTupleSchema()
                .getColumnSchemas()
                .stream()
                .map(this::renameQualifier)
                .collect(Collectors.toList()));
        
        return builder.build();
    }

    private ColumnSchema renameQualifier(ColumnSchema cs) {
        return ImmutableColumnSchema.builder()
                .from(cs)
                .qualifierAliases(getAliases())
                .build();
    }

    @Override
    public Stream<Tuple> getTupleStream() {
        return getSourceRelation().getTupleStream();
    }
}
