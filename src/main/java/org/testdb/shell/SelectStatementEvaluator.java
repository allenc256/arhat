package org.testdb.shell;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.relation.Relation;

import com.google.common.base.Preconditions;

public class SelectStatementEvaluator {
    public void evaluate(InMemoryDatabase database, SelectStatementContext ctx) {
        String tableName = ctx.selectStatementFromClause().ID().getText();
        Relation relation = database.getTables().get(tableName);
        
        Preconditions.checkState(
                relation != null,
                "Relation '%s' does not exist.",
                tableName);
        
        RelationRenderer.builder()
                .relation(relation)
                .build()
                .render();
    }
}
