package org.testdb.shell;

import java.util.List;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.ColumnDefinitionContext;
import org.testdb.parse.SQLParser.CreateTableStatementContext;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableColumnSchema;
import org.testdb.relation.ImmutableTupleSchema;
import org.testdb.relation.LexicographicTupleOrdering;
import org.testdb.relation.SortedMultisetRelation;
import org.testdb.relation.TupleSchema;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultiset;

public class CreateTableStatementEvaluator {
    public void evaluate(InMemoryDatabase database, CreateTableStatementContext ctx) {
        String tableName = ctx.ID().getText();
        List<ColumnSchema> tableColumns = getColumnSchemas(ctx);
        
        Preconditions.checkState(
                !database.getTables().containsKey(tableName),
                "Table %s already exists.",
                tableName);
        
        
        TupleSchema tupleSchema = ImmutableTupleSchema.builder()
                .columnSchemas(tableColumns)
                .build();
        
        SortedMultisetRelation table = SortedMultisetRelation.builder()
                .tupleSchema(tupleSchema)
                .tuplesSortedMultiset(TreeMultiset.create(LexicographicTupleOrdering.INSTANCE))
                .build();
        
        database.getTables().put(tableName, table);
    }

    private List<ColumnSchema> getColumnSchemas(CreateTableStatementContext ctx) {
        CreateTableVisitor visitor = new CreateTableVisitor();
        ctx.accept(visitor);
        return visitor.columnSchemas;
    }
    
    private static class CreateTableVisitor extends SQLBaseVisitor<Void> {
        private final List<ColumnSchema> columnSchemas = Lists.newArrayList();
        
        @Override
        public Void visitColumnDefinition(ColumnDefinitionContext ctx) {
            ImmutableColumnSchema.Builder builder = ImmutableColumnSchema.builder()
                    .index(columnSchemas.size())
                    .name(ctx.ID().getText());

            switch (ctx.type.getType()) {
            case SQLParser.STRING:
                builder.type(String.class);
                break;
            case SQLParser.INTEGER:
                builder.type(Integer.class);
                break;
            default:
                throw new IllegalStateException("Unrecognized token type.");
            }

            columnSchemas.add(builder.build());
            
            return null;
        }
    }
}
