package org.testdb.relation;

import java.util.List;

import org.immutables.value.Value;

import com.google.common.base.Preconditions;

@Value.Immutable
public abstract class AbstractProjectedRelation implements Relation {
    /**
     * Describes how the source schema should map to the target schema.
     * <p>
     * The ith index of this list describes what column from the source schema
     * should map to the ith column of the target schema. Specifically, it
     * should contain a column index from the source schema.
     */
    public abstract List<Integer> getSchemaMapping();
    
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
                return ImmutableProjectedTuple.builder()
                        .schema(getTupleSchema())
                        .schemaMapping(getSchemaMapping())
                        .sourceTuple(delegate().next())
                        .build();
            }
        };
    }

    @Value.Check
    protected void check() {
        TupleSchema sourceSchema = getSourceRelation().getTupleSchema();
        TupleSchema targetSchema = getTupleSchema();
        
        Preconditions.checkState(
                getSchemaMapping().size() == targetSchema.size(),
                "Size of mapping must match size of target schema.");
        
        for (int targetIdx = 0; targetIdx < getSchemaMapping().size(); ++targetIdx) {
            Integer sourceIdx = getSchemaMapping().get(targetIdx);
            Preconditions.checkState(
                    sourceIdx != null && sourceIdx >= 0 && sourceIdx < sourceSchema.size(),
                    "Invalid source index %d.",
                    sourceIdx);
            
            ColumnSchema source = sourceSchema.getColumnSchema(sourceIdx);
            ColumnSchema target = targetSchema.getColumnSchema(targetIdx);
            Preconditions.checkState(
                    source.getType().equals(target.getType()),
                    "Columns have mismatched types (source %s, target %s).",
                    source,
                    target);
        }
    }
}
