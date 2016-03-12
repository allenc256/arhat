package org.testdb.shell;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.DropTableStatementContext;

import com.google.common.base.Preconditions;

public class DropTableStatementEvaluator {
    public void evaluate(InMemoryDatabase database, DropTableStatementContext ctx) {
        String tableName = ctx.ID().getText().toLowerCase();
        
        Preconditions.checkState(
                database.getTables().containsKey(tableName),
                "Cannot drop non-existing table '%s'.",
                tableName);
        
        database.getTables().remove(tableName);
    }
}
