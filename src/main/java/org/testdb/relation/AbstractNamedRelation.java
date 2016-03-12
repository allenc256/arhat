package org.testdb.relation;

import java.util.stream.Collectors;

import org.immutables.value.Value;

// N.B., this wrapper should work even when the specified name is absent ---
// this is useful for removing all qualifiers from the schema of the
// relation. For example, this is used in sub-selects without explicitly
// specified names.
@Value.Immutable
public abstract class AbstractNamedRelation implements Relation {
    public abstract Relation getSourceRelation();
    
    @Value.Derived
    @Override
    public TupleSchema getTupleSchema() {
        ImmutableTupleSchema.Builder builder = ImmutableTupleSchema.builder()
                .from(getSourceRelation().getTupleSchema());
        
        builder.columnSchemas(getSourceRelation()
                .getTupleSchema()
                .getColumnSchemas()
                .stream()
                .map(cs -> {
                    QualifiedName newName = ImmutableQualifiedName.builder()
                            .from(cs.getQualifiedName())
                            .qualifier(getName())
                            .build();
                    return ImmutableColumnSchema.builder()
                            .from(cs)
                            .qualifiedName(newName)
                            .build();
                })
                .collect(Collectors.toList()));
        
        return builder.build();
    }

    @Override
    public Cursor<Tuple> getTuples() {
        return getSourceRelation().getTuples();
    }
}
