package org.testdb.statement;

import org.testdb.relation.Relation;

public interface SqlSelectStatement extends SqlStatement {
    Relation getRelation();

    @Override
    default <T> T accept(SqlStatementVisitor<T> visitor) {
        return visitor.visitSelectStatement(this);
    }
}
