package org.testdb.shell;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.DropTableStatementContext;
import org.testdb.parse.SqlParseException;

public class DropTableStatementEvaluator {
    public void evaluate(InMemoryDatabase database, DropTableStatementContext ctx) {
        String tableName = ctx.ID().getText().toLowerCase();
        
        if (!database.getTables().containsKey(tableName)) {
            throw SqlParseException.create(
                    ctx.ID().getSymbol(),
                    "cannot drop non-existing table '%s'.",
                    tableName);
        }
        
        database.getTables().remove(tableName);
    }
}
