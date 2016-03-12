package org.testdb.shell;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.parse.statement.SelectStatementParser;
import org.testdb.relation.Relation;

public class SelectStatementEvaluator {
    public void evaluate(InMemoryDatabase database, SelectStatementContext ctx) {
        SelectStatementParser parser = new SelectStatementParser();
        Relation relation = parser.parse(database, ctx);
        RelationRenderer.builder()
                .relation(relation)
                .build()
                .render();
    }
}
