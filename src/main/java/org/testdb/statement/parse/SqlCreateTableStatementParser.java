package org.testdb.statement.parse;

import java.util.List;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser.ColumnDefinitionContext;
import org.testdb.parse.SQLParser.CreateTableStatementContext;
import org.testdb.parse.SqlParseException;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableColumnSchema;
import org.testdb.relation.ImmutableTupleSchema;
import org.testdb.relation.TupleSchema;
import org.testdb.statement.ImmutableSqlCreateTableStatement;
import org.testdb.statement.SqlCreateTableStatement;
import org.testdb.type.SqlType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

class SqlCreateTableStatementParser {
    public SqlCreateTableStatement parse(InMemoryDatabase database, CreateTableStatementContext ctx) {
        String tableName = ctx.ID().getText().toLowerCase();
        List<ColumnSchema> tableColumns = getColumnSchemas(tableName, ctx);
        
        if (database.getTables().containsKey(tableName)) {
            throw SqlParseException.create(
                    ctx.ID().getSymbol(),
                    "relation '%s' already exists.",
                    tableName);
        }
        
        TupleSchema tupleSchema = ImmutableTupleSchema.builder()
                .columnSchemas(tableColumns)
                .build();
        
        return ImmutableSqlCreateTableStatement.builder()
                .database(database)
                .tableName(tableName)
                .tupleSchema(tupleSchema)
                .build();
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
                    .qualifierAliases(ImmutableSet.of(tableName))
                    .name(ctx.ID().getText().toLowerCase())
                    .type(SqlType.valueOf(ctx.type.getText().toUpperCase()))
                    .build());
            return null;
        }
    }
}
