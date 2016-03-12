package org.testdb.shell;

import java.util.List;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser.ColumnDefinitionContext;
import org.testdb.parse.SQLParser.CreateTableStatementContext;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableColumnSchema;
import org.testdb.relation.ImmutableQualifiedName;
import org.testdb.relation.ImmutableTupleSchema;
import org.testdb.relation.LexicographicTupleOrdering;
import org.testdb.relation.SortedMultisetRelation;
import org.testdb.relation.TupleSchema;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultiset;

public class CreateTableStatementEvaluator {
    public void evaluate(InMemoryDatabase database, CreateTableStatementContext ctx) {
        String tableName = ctx.ID().getText();
        List<ColumnSchema> tableColumns = getColumnSchemas(tableName, ctx);
        
        Preconditions.checkState(
                !database.getTables().containsKey(tableName),
                "Table %s already exists.",
                tableName);
        
        TupleSchema tupleSchema = ImmutableTupleSchema.builder()
                .columnSchemas(tableColumns)
                .build();
        
        SortedMultisetRelation table = SortedMultisetRelation.builder()
                .name(tableName)
                .tupleSchema(tupleSchema)
                .tuplesSortedMultiset(TreeMultiset.create(LexicographicTupleOrdering.INSTANCE))
                .build();
        
        database.getTables().put(tableName, table);
    }

    private List<ColumnSchema> getColumnSchemas(String tableName,
                                                CreateTableStatementContext ctx) {
        CreateTableVisitor visitor = new CreateTableVisitor(tableName);
        ctx.accept(visitor);
        return visitor.columnSchemas;
    }
    
    private static class CreateTableVisitor extends SQLBaseVisitor<Void> {
        private final List<ColumnSchema> columnSchemas;
        private final String tableName;
        
        private CreateTableVisitor(String tableName) {
            this.tableName = tableName;
            this.columnSchemas = Lists.newArrayList();
        }

        @Override
        public Void visitColumnDefinition(ColumnDefinitionContext ctx) {
            columnSchemas.add(ImmutableColumnSchema.builder()
                    .index(columnSchemas.size())
                    .qualifiedName(ImmutableQualifiedName.of(tableName, ctx.ID().getText()))
                    .type(SqlType.valueOf(ctx.type.getText().toUpperCase()))
                    .build());
            return null;
        }
    }
}
