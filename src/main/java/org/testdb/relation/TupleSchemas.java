package org.testdb.relation;

public class TupleSchemas {
    private TupleSchemas() {
        // empty
    }
    
    public static TupleSchema concat(TupleSchema... tupleSchemas) {
        ImmutableTupleSchema.Builder builder = ImmutableTupleSchema.builder();
        int count = 0;
        
        for (TupleSchema ts : tupleSchemas) {
            for (ColumnSchema cs : ts.getColumnSchemas()) {
                builder.addColumnSchemas(ImmutableColumnSchema.builder()
                        .from(cs)
                        .index(count++)
                        .build());
            }
        }
        
        return builder.build();
    }
}
