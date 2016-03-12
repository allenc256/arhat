package org.testdb.statement.parse;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.DropTableStatementContext;
import org.testdb.parse.SqlParseException;
import org.testdb.statement.ImmutableSqlDropTableStatement;
import org.testdb.statement.SqlDropTableStatement;

class SqlDropTableStatementParser {
    public SqlDropTableStatement parse(InMemoryDatabase database,
                                       DropTableStatementContext ctx) {
        String tableName = ctx.ID().getText().toLowerCase();
        
        if (!database.getTables().containsKey(tableName)) {
            throw SqlParseException.create(
                    ctx.ID().getSymbol(),
                    "cannot drop non-existing table '%s'.",
                    tableName);
        }
        
        return ImmutableSqlDropTableStatement.builder()
                .database(database)
                .tableName(tableName)
                .build();
    }
}
