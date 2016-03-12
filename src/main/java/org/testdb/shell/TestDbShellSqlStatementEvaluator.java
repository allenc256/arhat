package org.testdb.shell;

import org.testdb.statement.AbstractSqlStatementEvaluator;
import org.testdb.statement.SqlSelectStatement;

public class TestDbShellSqlStatementEvaluator extends AbstractSqlStatementEvaluator {
    @Override
    public Void visitSelectStatement(SqlSelectStatement statement) {
        ImmutablePrettyPrintRelationRenderer.builder()
                .relation(statement.getRelation())
                .build()
                .render();
        return null;
    }
}
